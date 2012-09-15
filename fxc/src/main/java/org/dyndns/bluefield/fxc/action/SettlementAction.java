package org.dyndns.bluefield.fxc.action;

import java.util.Date;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.service.SettlementService;
import org.dyndns.bluefield.fxc.service.SettlementService.SettleResult;
import org.dyndns.bluefield.fxc.util.PriceUtil;
import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.Path;

@ActionClass
@Path("settlement")
public class SettlementAction {

	@Resource
	private SettlementService settlementService;

	public Date fromDt;
	public Integer balance;
	public Integer profit;
	public String balanceDiff;
	public String profitDiff;

	public ActionResult index() {

		SettleResult diff = settlementService.getDiffUntilNow();
		fromDt = diff.lastSettlementDt;
		balance = diff.balance;
		profit = diff.profit;

		balanceDiff = PriceUtil.separateComma(diff.balanceDiff.toString());
		if (diff.balanceDiff >= 0) balanceDiff = "+" + balanceDiff;
		profitDiff = PriceUtil.separateComma(diff.profitDiff.toString());
		if (diff.profitDiff >= 0) profitDiff = "+" + profitDiff;


		return new Forward("index.jsp");
	}
}
