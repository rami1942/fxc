package org.dyndns.bluefield.fxc.action;

import java.util.Date;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.service.ConfigService;
import org.dyndns.bluefield.fxc.service.SettlementService;
import org.dyndns.bluefield.fxc.service.SettlementService.SettleResult;
import org.dyndns.bluefield.fxc.util.PriceUtil;
import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.Path;
import org.seasar.cubby.action.Redirect;

@ActionClass
@Path("settlement")
public class SettlementAction {

	@Resource
	private SettlementService settlementService;

	@Resource
	private ConfigService configService;

	public Date fromDt;
	public Integer balance;
	public Integer profit;
	public Integer swapPoint;
	public String balanceDiff;
	public String profitDiff;

	public Integer numRepeat;

	public String accessKey;

	public ActionResult index() {
		accessKey = configService.getByString("auth_key");

		SettleResult diff = settlementService.getDiffUntilNow();
		fromDt = diff.lastSettlementDt;
		balance = diff.balance;
		profit = diff.profit;
		swapPoint = diff.swapPoint;
		numRepeat = diff.numRepeat;


		balanceDiff = PriceUtil.separateComma(diff.balanceDiff.toString());
		if (diff.balanceDiff >= 0) balanceDiff = "+" + balanceDiff;
		profitDiff = PriceUtil.separateComma(diff.profitDiff.toString());
		if (diff.profitDiff >= 0) profitDiff = "+" + profitDiff;

		return new Forward("index.jsp");
	}

	public ActionResult update() {
		accessKey = configService.getByString("auth_key");
		settlementService.update();
		return new Redirect("./");
	}
}
