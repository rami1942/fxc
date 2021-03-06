package org.dyndns.bluefield.fxc.action;

import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.Position;
import org.dyndns.bluefield.fxc.entity.ShortTrap;
import org.dyndns.bluefield.fxc.service.ConfigService;
import org.dyndns.bluefield.fxc.service.PositionService;
import org.dyndns.bluefield.fxc.service.ToggleTpRequestService;
import org.dyndns.bluefield.fxc.service.PositionService.LongInfo;
import org.dyndns.bluefield.fxc.service.PositionService.LosscutInfo;
import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.Path;
import org.seasar.cubby.action.Redirect;
import org.seasar.cubby.action.RequestParameter;

@ActionClass
@Path("/")
public class IndexAction {
	@Resource
	private PositionService positionService;
	@Resource
	private ConfigService configService;
	@Resource
	private ToggleTpRequestService toggleTpRequestService;

	@RequestParameter
	public String price;

	@RequestParameter
	public Integer ticketNo;

	@RequestParameter
	public Integer tpFlag;

	public List<ShortTrap> shorts;
	public List<Position> longs;
	public Integer eachLots;
	public Integer numTraps;
	public Double longAverage;
	public String accessKey;
	public Double lossCutPrice;
	public Double lossCutLevel;

	public ActionResult index() {
		shorts = positionService.getShortTraps();
		longs = positionService.getKKWBody();
		eachLots = configService.getLotsByTrap();
		accessKey = configService.getAuthKey();

		LosscutInfo lc = positionService.calcLosscutRate();
		lossCutPrice = lc.price;
		lossCutLevel = lc.level;

		LongInfo info = positionService.calcTraps(longs);
		longAverage = info.avg;
		numTraps = info.numTraps;

		return new Forward("index.jsp");
	}

	public ActionResult extendUp() {
		ShortTrap sp = positionService.getMaxShortPosition();
		Double width = configService.getTrapWidth();
		positionService.insert(sp.openPrice + width);

		return new Redirect("./");
	}

	public ActionResult shortenUp() {
		ShortTrap sp = positionService.getMaxShortPosition();
		positionService.delete(sp);
		return new Redirect("./");
	}

	public ActionResult extendDown() {
		ShortTrap sp = positionService.getMinShortPosition();
		Double width = configService.getTrapWidth();
		positionService.insert(sp.openPrice - width);

		return new Redirect("./");
	}

	public ActionResult shortenDown() {
		ShortTrap sp = positionService.getMinShortPosition();
		positionService.delete(sp);

		return new Redirect("./");
	}

	public ActionResult freezePosition() {
		positionService.setPositionType(ticketNo, 0);
		return new Redirect("./");
	}

	public ActionResult toggleTp() {
		toggleTpRequestService.toggleTp(ticketNo, tpFlag);
		return new Redirect("./?ak=" + configService.getAuthKey());
	}
}