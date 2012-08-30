package org.dyndns.bluefield.fxc.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.LongPosition;
import org.dyndns.bluefield.fxc.entity.ShortPosition;
import org.dyndns.bluefield.fxc.service.ConfigService;
import org.dyndns.bluefield.fxc.service.PositionService;
import org.dyndns.bluefield.fxc.service.PositionService.LongInfo;
import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.Path;
import org.seasar.cubby.action.Redirect;

@ActionClass
@Path("/")
public class IndexAction {
	@Resource
	private PositionService positionService;
	@Resource
	private ConfigService configService;

	public List<ShortPosition> shorts;
	public Integer eachLots;
	public Integer numTraps;
	public Double longAverage;
	public List<LongPosition> longs;

	public String currentPrice;
	public String currentDatetime;

	public Double currentPricePos;
	public String basePrice;
	public Double baseLine;
	public String prices;
	public String positions;
	public String longPositions;

	public ActionResult index() {
		shorts = positionService.getShortPositions();		
		eachLots = configService.getByInteger("lots");
		longs = positionService.getLongPositions();
		
		LongInfo info = positionService.calcTraps(longs);
		longAverage = info.avg;
		numTraps = info.numTraps;

		return new Forward("index.jsp");
	}

	public ActionResult extendUp() {
		ShortPosition sp = positionService.getMaxShortPosition();
		Double width = configService.getByDouble("trap_width");		
		positionService.insert(sp.openPrice + width);

		return new Redirect("./");
	}

	public ActionResult shortenUp() {
		ShortPosition sp = positionService.getMaxShortPosition();
		positionService.delete(sp);
		return new Redirect("./");
	}

	public ActionResult extendDown() {
		ShortPosition sp = positionService.getMinShortPosition();
		Double width = configService.getByDouble("trap_width");		
		positionService.insert(sp.openPrice - width);

		return new Redirect("./");
	}

	public ActionResult shortenDown() {
		ShortPosition sp = positionService.getMinShortPosition();
		positionService.delete(sp);

		return new Redirect("./");
	}

	public ActionResult chart() {
		shorts = positionService.getShortPositions();
		longs = positionService.getLongPositions();

		Double trapWidth = configService.getByDouble("trap_width");

		// 平均建値・トラップ本数の算出
		eachLots = configService.getByInteger("lots");

		LongInfo info = positionService.calcTraps(longs);
		longAverage = info.avg;
		numTraps = info.numTraps;

		basePrice = longAverage.toString();
		baseLine = (shorts.get(0).openPrice - longAverage) / trapWidth;

		// 現在価格位置の算出
		Double curPrice = configService.getByDouble("current_price");
		currentPricePos = (shorts.get(0).openPrice - curPrice) / trapWidth;
		currentPrice = String.format("%3.3f", curPrice);

		// 現在時刻
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		currentDatetime = sdf.format(new Date());

		// 目盛
		StringBuilder buf = new StringBuilder();
		int i = 0;
		for (ShortPosition sp : shorts) {
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
		for (ShortPosition sp : shorts) {
			buf.append(sp.isReal);
			buf.append(',');
		}
		positions = buf.toString();

		// ロングポジション
		buf = new StringBuilder();
		for (LongPosition lp : longs) {
			if (lp.isWideBody == 0) continue;
			double d = (shorts.get(0).openPrice - lp.openPrice) / trapWidth;
			buf.append(d);
			buf.append(',');
		}
		longPositions = buf.toString();

		return new Forward("chart.jsp");
	}
}