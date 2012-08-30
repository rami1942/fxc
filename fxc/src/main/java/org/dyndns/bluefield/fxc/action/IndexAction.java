package org.dyndns.bluefield.fxc.action;

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
import org.seasar.cubby.action.RequestParameter;

@ActionClass
@Path("/")
public class IndexAction {
	@Resource
	private PositionService positionService;
	@Resource
	private ConfigService configService;

	@RequestParameter
	public String price;
	
	public List<ShortPosition> shorts;
	public List<LongPosition> longs;
	public List<LongPosition> freezes;
	public Integer eachLots;
	public Integer numTraps;
	public Double longAverage;

	public ActionResult index() {
		shorts = positionService.getShortPositions();		
		longs = positionService.getLongPositions();
		freezes = positionService.getFreezeLongs();
		eachLots = configService.getByInteger("lots");
		
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
	
	public ActionResult freezePosition() {
		positionService.setToFreeze(price);
		return new Redirect("./");
	}
	
	public ActionResult unfreezePosition() {
		positionService.setToUnfreeze(price);
		return new Redirect("./");
	}
}