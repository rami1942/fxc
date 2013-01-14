package org.dyndns.bluefield.fxc.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.PositionHistory;
import org.dyndns.bluefield.fxc.service.ConfigService;
import org.dyndns.bluefield.fxc.service.SettlementService;
import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.RequestParameter;

@ActionClass
public class SettlementAction {

	@Resource
	private SettlementService settlementService;

	@Resource
	private ConfigService configService;

	public List<PositionHistory> history;

	public String accessKey;

	@RequestParameter
	public String date;

	public ActionResult index() {
		accessKey = configService.getAuthKey();

		if (date == null) {
			SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd");
			date = sdt.format(new Date());
		}
		history = settlementService.getHistory(date);
		return new Forward("index.jsp");
	}
}
