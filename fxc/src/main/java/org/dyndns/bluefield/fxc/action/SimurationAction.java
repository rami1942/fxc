package org.dyndns.bluefield.fxc.action;

import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.Position;
import org.dyndns.bluefield.fxc.entity.SimuratePosition;
import org.dyndns.bluefield.fxc.service.ConfigService;
import org.dyndns.bluefield.fxc.service.PositionService;
import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.RequestParameter;

@ActionClass
public class SimurationAction {

	@Resource
	private PositionService positionService;

	@Resource
	private ConfigService configService;

	@RequestParameter
	public Double targetRate;

	public List<SimuratePosition> positions;
	public Integer balance;
	public Integer proLossTotal;
	public Integer requiredMargin;
	public Double marginPer;

	public Double longLots;
	public Double shortLots;

	public String accessKey;

	public List<Position> exportPos;

	public ActionResult index() {

		if (targetRate == null) {
			targetRate = configService.getCurrentPrice();
		}

		positions = positionService.filteredPositions(targetRate);
		accessKey = configService.getAuthKey();

		longLots = shortLots = 0.0;

		balance = configService.getBalance().intValue();

		proLossTotal = 0;
		for (SimuratePosition p : positions) {
			if (p.isActive()) {
				if (p.proLoss != null) proLossTotal += p.proLoss;
				if (p.isLong()) longLots += p.lots;
				else shortLots += p.lots;
			} else {
				if (p.proLoss != null) balance += p.proLoss;
			}

		}
		longLots = Math.round(longLots * 100.0) / 100.0;
		shortLots = Math.round(shortLots * 100.0) / 100.0;


		requiredMargin = positionService.getMargin(positions);

		marginPer = ((double)balance + (double)proLossTotal) /requiredMargin * 100;
		marginPer = Math.round(marginPer * 100.0) / 100.0;


		return new Forward("index.jsp");
	}

	public ActionResult export() {
		exportPos = positionService.getPositions();
		return new Forward("export.jsp");
	}
}
