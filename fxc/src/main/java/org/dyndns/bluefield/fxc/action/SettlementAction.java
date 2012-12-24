package org.dyndns.bluefield.fxc.action;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.DiscPosition;
import org.dyndns.bluefield.fxc.entity.ReservedProfit;
import org.dyndns.bluefield.fxc.entity.ShortTrap;
import org.dyndns.bluefield.fxc.service.ConfigService;
import org.dyndns.bluefield.fxc.service.HistoryService;
import org.dyndns.bluefield.fxc.service.PositionService;
import org.dyndns.bluefield.fxc.service.SettlementService;
import org.dyndns.bluefield.fxc.service.PositionService.LongInfo;
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

	@Resource
	private HistoryService historyService;

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

	public List<DiscPosition> discs;

	public Double lotsShortExit;
	public Double lotsShortVOpenPrice;
	public Double virtualOpenPrice;
	public Double discLongBasePrice;
	public Double lotsLong;

	public Double currentRate;

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
		ShortTrap st = positionService.getMaxShortPosition();
		if (st == null) return 0;
		double exitPrice = st.openPrice;
		double curPrice = configService.getCurrentPrice();
		double lots = reserve / (exitPrice - curPrice);
		lots = Math.floor(lots / 100) / 1000;
		return lots;
	}


	public ActionResult index() {
		accessKey = configService.getAuthKey();

		Double currentPrice = configService.getCurrentPrice();

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
		virtualPriceReservation = configService.getVpReserve();
		profitReservation = configService.getProfitReservation();
		oneLinePrice = positionService.calcOneLinePrice();

		// ヘッジ可能量
		int exp = positionService.exitProfit();
		shAmount = PriceUtil.separateComma(Integer.toString(exp + virtualPriceReservation));
		hedgeLots = calcHedgeLots(exp + virtualPriceReservation);

		// 確保益
		discs = positionService.discPositions(currentPrice);
		for (DiscPosition d : discs) {
			if (d.slPrice == 0.0) d.slPrice = null;
			if (d.isLong) {
				if (d.slPrice != null) {
					d.margin = (int)Math.round(d.openPrice * 0.04 * d.lots * 100000);
					d.slProfit = (int)Math.round((d.slPrice - d.openPrice) * d.lots * 100000 + d.swapPoint);
				}
			} else {
				d.margin = null;
				if (d.slPrice != null) {
					d.slProfit = (int)Math.round((d.openPrice - d.slPrice) * d.lots * 100000);
				} else {
					d.slProfit = 0;
				}
			}
		}

		// 余裕額
		remain = diff.kkwProfit;
		reservedProfits = settlementService.reservedProfits();
		for (ReservedProfit rp : reservedProfits) {
			remain += rp.amount;
		}

		remain -= virtualPriceReservation;
		remain -= profitReservation;

		for (DiscPosition d : discs) {
			remain += d.slProfit;
		}

		baseDt = configService.getBaseDt();

		//出口建て量
		ShortTrap st = positionService.getMaxShortPosition();
		if (st == null) {
			lotsShortExit = null;
		} else {
			lotsShortExit = remain / (st.openPrice - currentPrice ) / 100000;
			if (lotsShortExit < 0.0) {
				lotsShortExit = null;
			} else {
				lotsShortExit = Math.round(lotsShortExit * 1000.0) / 1000.0;
			}
		}

		// 本体まで建て量
		LongInfo info = positionService.calcTraps();
		lotsShortVOpenPrice = remain / (info.avg - info.virtualPriceOffset - currentPrice) / 100000;
		if (lotsShortVOpenPrice < 0.0) {
			lotsShortVOpenPrice = null;
		} else {
			lotsShortVOpenPrice = Math.round(lotsShortVOpenPrice * 1000.0) / 1000.0;
			virtualOpenPrice = Math.round((info.avg - info.virtualPriceOffset) * 1000.0) / 1000.0;
		}

		// ロング建て量
		discLongBasePrice = configService.getDiscLongBasePrice();
		lotsLong = remain / (currentPrice - discLongBasePrice) / 100000;
		if (lotsLong < 0.0) {
			lotsLong = null;
		} else {
			lotsLong = Math.round(lotsLong * 1000.0) / 1000.0;
		}

		currentRate = configService.getCurrentPrice();

		return new Forward("index.jsp");
	}

	public ActionResult update() {
		accessKey = configService.getAuthKey();
		settlementService.update();
		return new Redirect("./?ak=" + accessKey);
	}

	@Validation(rules="validation", errorPage="index.jsp")
	public ActionResult reserve() {
		accessKey = configService.getAuthKey();
		settlementService.reserve(reserveAmount, reserveDesc);
		return new Redirect("./?ak=" + accessKey);
	}

	public ActionResult delete() {
		accessKey = configService.getAuthKey();
		if (id == null) return new Redirect("./");
		settlementService.unReserve(id);
		return new Redirect("./?ak=" + accessKey);
	}

	public ActionResult setVirtualPriceReservation() {
		accessKey = configService.getAuthKey();
		if (virtualPriceReservation != null) {
			configService.setVpReserve(virtualPriceReservation);
			historyService.insertVirtualPrice(virtualPriceReservation);
		}
		return new Redirect("./?ak=" + accessKey);
	}

	public ActionResult setBaseDt() {
		accessKey = configService.getAuthKey();
		configService.setBaseDt(baseDt);
		return new Redirect("./?ak=" + accessKey);
	}

	public ActionResult setProfitReservation() {
		accessKey = configService.getAuthKey();
		configService.setProfitReservation(profitReservation);
		return new Redirect("./?ak=" + accessKey);
	}
}
