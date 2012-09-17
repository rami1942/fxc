package org.dyndns.bluefield.fxc.action;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.ReservedProfit;
import org.dyndns.bluefield.fxc.service.ConfigService;
import org.dyndns.bluefield.fxc.service.PositionService;
import org.dyndns.bluefield.fxc.service.SettlementService;
import org.dyndns.bluefield.fxc.service.SettlementService.SettleResult;
import org.dyndns.bluefield.fxc.util.PriceUtil;
import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.Path;
import org.seasar.cubby.action.Redirect;
import org.seasar.cubby.action.RequestParameter;
import org.seasar.cubby.action.Validation;
import org.seasar.cubby.validator.DefaultValidationRules;
import org.seasar.cubby.validator.ValidationRules;
import org.seasar.cubby.validator.validators.NumberValidator;
import org.seasar.cubby.validator.validators.RequiredValidator;

@ActionClass
@Path("settlement")
public class SettlementAction {

	@Resource
	private SettlementService settlementService;

	@Resource
	private ConfigService configService;

	@Resource
	private PositionService positionService;

	public Date fromDt;
	public Integer balance;
	public Integer profit;
	public Integer swapPoint;
	public String balanceDiff;
	public String profitDiff;

	public Integer numRepeat;
	public String kkwProfit;

	public String accessKey;

	public List<ReservedProfit> reservedProfits;
	public Integer remain;

	public String exitProfit;

	public String shAmount;
	public Double hedgeLots;

	@RequestParameter
	public Integer reserveAmount;
	@RequestParameter
	public String reserveDesc;

	@RequestParameter
	public Integer id;

	@RequestParameter
	public Integer virtualPriceReservation;

	public ValidationRules validation = new DefaultValidationRules() {
		@Override
		public void initialize() {
			add("reserveAmount", new RequiredValidator(), new NumberValidator());
			add("reserveDesc", new RequiredValidator());
		}
	};

	private double calcHedgeLots(int reserve) {
		double exitPrice = positionService.getMaxShortPosition().openPrice;
		double curPrice = configService.getByDouble("current_price");
		System.out.println("exit = " + exitPrice + " cur = " + curPrice);
		double lots = reserve / (exitPrice - curPrice);
		lots = Math.floor(lots / 100) / 1000;
		return lots;
	}

	public ActionResult index() {
		accessKey = configService.getByString("auth_key");

		SettleResult diff = settlementService.getDiffUntilNow();
		fromDt = diff.lastSettlementDt;
		balance = diff.balance;
		profit = diff.profit;
		swapPoint = diff.swapPoint;
		numRepeat = diff.numRepeat;
		kkwProfit = PriceUtil.separateComma(diff.kkwProfit.toString());


		balanceDiff = PriceUtil.separateComma(diff.balanceDiff.toString());
		if (diff.balanceDiff >= 0) balanceDiff = "+" + balanceDiff;
		profitDiff = PriceUtil.separateComma(diff.profitDiff.toString());
		if (diff.profitDiff >= 0) profitDiff = "+" + profitDiff;

		int exp = positionService.exitProfit();
		exitProfit = PriceUtil.separateComma(Integer.toString(exp));

		virtualPriceReservation = configService.getByInteger("vp_reserve");

		shAmount = PriceUtil.separateComma(Integer.toString(exp + virtualPriceReservation));
		// ヘッジ可能量の計算
		hedgeLots = calcHedgeLots(exp + virtualPriceReservation);

		// 余裕額の計算
		reservedProfits = settlementService.reservedProfits();
		remain = diff.kkwProfit;
		for (ReservedProfit rp : reservedProfits) {
			remain -= rp.amount;
		}
		remain += exp;
		remain -= virtualPriceReservation;

		return new Forward("index.jsp");
	}

	public ActionResult update() {
		accessKey = configService.getByString("auth_key");
		settlementService.update();
		return new Redirect("./?ak=" + accessKey);
	}

	@Validation(rules="validation", errorPage="index.jsp")
	public ActionResult reserve() {
		accessKey = configService.getByString("auth_key");
		settlementService.reserve(reserveAmount, reserveDesc);
		return new Redirect("./?ak=" + accessKey);
	}

	public ActionResult delete() {
		accessKey = configService.getByString("auth_key");
		if (id == null) return new Redirect("./");
		settlementService.unReserve(id);
		return new Redirect("./?ak=" + accessKey);
	}

	public ActionResult setVirtualPriceReservation() {
		accessKey = configService.getByString("auth_key");

		if (virtualPriceReservation != null) {
			configService.set("vp_reserve", virtualPriceReservation.toString());
		}

		return new Redirect("./?ak=" + accessKey);
	}
}
