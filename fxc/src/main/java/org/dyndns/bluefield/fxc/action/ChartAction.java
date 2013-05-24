package org.dyndns.bluefield.fxc.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.DiscPosition;
import org.dyndns.bluefield.fxc.entity.Position;
import org.dyndns.bluefield.fxc.entity.ShortTrap;
import org.dyndns.bluefield.fxc.service.ConfigService;
import org.dyndns.bluefield.fxc.service.PositionService;
import org.dyndns.bluefield.fxc.service.PositionService.LongInfo;
import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.Path;

@ActionClass
@Path("chart")
public class ChartAction {
	@Resource
	private PositionService positionService;
	@Resource
	private ConfigService configService;

	public Integer numTraps;

	public String currentPrice;
	public String currentDatetime;

	public Double currentPricePos;
	public String basePrice;
	public Double baseLine;
	public String prices;

	public String truePrice;
	public Double trueLine;

	public String positions;
	public String longPositions;

	public String discPosType;
	public String discPosOP;
	public String discPosSL;
	public String discPosPrice;
	public String discPosWidth;

	public String accessKey;

	public ActionResult index() {
		List<ShortTrap> shorts = positionService.getShortTraps();
		List<Position> longs = positionService.getKKWBody();
		Double exitPrice = shorts.size() > 0 ? shorts.get(0).openPrice : 0.0;

		Double trapWidth = configService.getTrapWidth();
		Double baseOffset = configService.getBaseOffset();

		// 平均建値・トラップ本数の算出
		LongInfo info = positionService.calcTraps(longs);
		numTraps = info.numTraps;

		// 建値・仮想建値
		double longShift = info.virtualPriceOffset;
		double vp = info.avg - longShift;
		vp = Math.round(vp * 1000.0) / 1000.0;
		basePrice = Double.toString(vp);
		baseLine = (exitPrice - info.avg + longShift) / trapWidth + baseOffset;

		truePrice = Double.toString(info.avg);
		trueLine = (exitPrice - info.avg) / trapWidth + baseOffset;

		// 現在価格位置の算出
		Double curPrice = configService.getCurrentPrice();
		currentPricePos = (exitPrice - curPrice) / trapWidth;
		currentPrice = String.format("%3.3f", curPrice);

		// 現在時刻
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		currentDatetime = sdf.format(new Date());

		// 目盛
		StringBuilder buf = new StringBuilder();
		int i = 0;
		for (ShortTrap sp : shorts) {
			if (i % 2 == 0) {
				buf.append("'");
				buf.append(String.format("%3.3f", sp.openPrice));
				buf.append("',");
			}
			i++;
		}
		prices = buf.toString();

		// ショートポジション
		buf = new StringBuilder();
		for (ShortTrap sp : shorts) {
			buf.append(sp.isReal);
			buf.append(',');
		}
		positions = buf.toString();

		// ロングポジション
		buf = new StringBuilder();
		for (Position lp : longs) {
			double d = (exitPrice - lp.openPrice) / trapWidth;
			buf.append(d);
			buf.append(',');
		}
		longPositions = buf.toString();

		// 裁量ポジション
		buf = new StringBuilder();
		StringBuilder buf2 = new StringBuilder();
		StringBuilder buf3 = new StringBuilder();
		StringBuilder buf4 = new StringBuilder();
		StringBuilder buf5 = new StringBuilder();
		List<DiscPosition> discs = positionService.discPositions(curPrice);
		for (DiscPosition p : discs) {
			if (p.posType == 4) continue;
			if (p.isLong) {
				buf.append("true");
			} else {
				buf.append("false");
			}
			buf.append(',');

			double d;
			d = (exitPrice - p.openPrice) / trapWidth;
			buf2.append(d);
			buf2.append(',');

			if (p.slPrice == null || p.slPrice == 0.0) {
				buf3.append("null");
			} else {
				d = (exitPrice - p.slPrice) / trapWidth;
				buf3.append(d);
			}
			buf3.append(',');

			buf4.append(String.format("'%3.3f'", p.openPrice));
			buf4.append(',');

			int boxSize = 40;
			int w = (int)Math.round(p.lots * (boxSize / 2) / 1);
			if (w < 3) w = 3;
			if (w > boxSize / 2) w = boxSize / 2;

			buf5.append(w);
			buf5.append(',');
		}
		discPosType = buf.toString();
		discPosOP = buf2.toString();
		discPosSL = buf3.toString();
		discPosPrice = buf4.toString();
		discPosWidth = buf5.toString();

		accessKey = configService.getAuthKey();

		return new Forward("index.jsp");
	}
}
