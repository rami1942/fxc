package org.dyndns.bluefield.fxc.action;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.Position;
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

	public String shAmount;
	public Double hedgeLots;

	public Integer oneLinePrice;
	
	public List<Position> hedges;
	
	@RequestParameter
	public Integer reserveAmount;
	@RequestParameter
	public String reserveDesc;

	@RequestParameter
	public Integer id;

	@RequestParameter
	public Integer virtualPriceReservation;
	
	@RequestParameter
	public String baseDt;

	@RequestParameter
	public Integer profitReservation;
	
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
		double lots = reserve / (exitPrice - curPrice);
		lots = Math.floor(lots / 100) / 1000;
		return lots;
	}
	

	public ActionResult index() {
		accessKey = configService.getByString("auth_key");

		// 前回差分
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

		// 仮想建値・確保益
		virtualPriceReservation = configService.getByInteger("vp_reserve");
		profitReservation = configService.getByInteger("profit_reservation");
		oneLinePrice = positionService.calcOneLinePrice();
		
		// ヘッジ可能量
		int exp = positionService.exitProfit();
		shAmount = PriceUtil.separateComma(Integer.toString(exp + virtualPriceReservation));
		hedgeLots = calcHedgeLots(exp + virtualPriceReservation);

		// SLによる確保益
		hedges = settlementService.calcHedgedFixedProfit();
		
		// 余裕額
		reservedProfits = settlementService.reservedProfits();
		remain = diff.kkwProfit;
		for (ReservedProfit rp : reservedProfits) {
			remain += rp.amount;
		}
		for (Position p : hedges) {
			remain += (int)Math.round(p.profit);
		}
		remain -= virtualPriceReservation;
		remain -= profitReservation;

		baseDt = configService.getByString("base_dt");
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
	
	public ActionResult setBaseDt() {
		accessKey = configService.getByString("auth_key");
		configService.set("base_dt", baseDt);
		return new Redirect("./?ak=" + accessKey);
	}
	
	public ActionResult setProfitReservation() {
		accessKey = configService.getByString("auth_key");
		configService.set("profit_reservation", profitReservation.toString());
		return new Redirect("./?ak=" + accessKey);		
	}
}
