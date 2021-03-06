package org.dyndns.bluefield.fxc.action;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.DiscPosition;
import org.dyndns.bluefield.fxc.entity.Position;
import org.dyndns.bluefield.fxc.entity.ReservedProfit;
import org.dyndns.bluefield.fxc.entity.ShortTrap;
import org.dyndns.bluefield.fxc.service.ConfigService;
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
@Path("position")
public class PositionAction {

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

	public Integer shAmount;
	public Integer exitRemain;
	public Double hedgeLots;

	public Integer oneLinePrice;

	public List<DiscPosition> discs;

	public Double lotsShortExit;
	public Double lotsShortVOpenPrice;
	public Double virtualOpenPrice;
	public Double discLongBasePrice;
	public Double lotsLong;

	public Double currentRate;
	public Double exitRate;

	public Double longsTotal;
	public Double shortsTotal;
	public Double longsKKW;
	public Double shortsHedge;

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

	@RequestParameter
	public Integer ticketNo;
	@RequestParameter
	public Integer selValue;

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
		currentRate = configService.getCurrentPrice();
		exitRate = positionService.exitRate();

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
		exitRemain = shAmount = exp + virtualPriceReservation;
		int exitUse = 0;

		longsTotal = shortsTotal = longsKKW = shortsHedge = 0.0;

		// 確保益
		discs = positionService.discPositions(currentRate);
		for (DiscPosition d : discs) {
			if (d.slPrice == 0.0) d.slPrice = null;

			if (d.isLong) {
				if (d.slPrice != null) {
					d.slProfit = (int)Math.round((d.slPrice - d.openPrice) * d.lots * 100000 + d.swapPoint);
				}
			} else {
				if (d.posType == 4) {
					d.slProfit = null;
				} else if (d.slPrice != null) {
					d.slProfit = (int)Math.round((d.openPrice - d.slPrice) * d.lots * 100000);
				} else {
					d.slProfit = 0;
				}
			}

			if (d.isLong) {
				d.realProfit = (int)Math.round((currentRate - d.openPrice) * d.lots * 100000 + d.swapPoint);
			} else {
				d.realProfit = (int)Math.round((d.openPrice - currentRate) * d.lots * 100000 + d.swapPoint);
			}

			if (d.posType == 5) {
				exitRemain +=d.slProfit;
				exitUse += d.slProfit;
			}

			if (d.isLong) {
				longsTotal += d.lots;
			} else {
				shortsTotal += d.lots;
			}
			if (!d.isLong && d.posType != 7) shortsHedge += d.lots;
		}

		for (Position p : positionService.getKKWBody()) {
			longsTotal += p.lots;
			longsKKW += p.lots;
		}

		longsTotal = Math.round(longsTotal * 1000.0) / 1000.0;
		longsKKW = Math.round(longsKKW * 1000.0) / 1000.0;
		shortsTotal = Math.round(shortsTotal * 1000.0) / 1000.0;
		shortsHedge = Math.round(shortsHedge * 1000.0) / 1000.0;

		// 出口益S
		hedgeLots = calcHedgeLots(exitRemain);

		// 余裕額
		remain = diff.kkwProfit - exitUse;
		reservedProfits = settlementService.reservedProfits();
		for (ReservedProfit rp : reservedProfits) {
			remain += rp.amount;
		}

		remain -= virtualPriceReservation;
		remain -= profitReservation;

		for (DiscPosition d : discs) {
			if (d.slProfit != null) remain += d.slProfit;
		}

		//出口建て量
		ShortTrap st = positionService.getMaxShortPosition();
		if (st == null) {
			lotsShortExit = null;
		} else {
			lotsShortExit = remain / (st.openPrice - currentRate ) / 100000;
			if (lotsShortExit < 0.0) {
				lotsShortExit = null;
			} else {
				lotsShortExit = Math.round(lotsShortExit * 1000.0) / 1000.0;
			}
		}

		// 本体まで建て量
		LongInfo info = positionService.calcTraps();
		lotsShortVOpenPrice = remain / (info.avg - info.virtualPriceOffset - currentRate) / 100000;
		if (lotsShortVOpenPrice < 0.0) {
			lotsShortVOpenPrice = null;
		} else {
			lotsShortVOpenPrice = Math.round(lotsShortVOpenPrice * 1000.0) / 1000.0;
			virtualOpenPrice = Math.round((info.avg - info.virtualPriceOffset) * 1000.0) / 1000.0;
		}

		// ロング建て量
		discLongBasePrice = configService.getDiscLongBasePrice();
		lotsLong = remain / (currentRate - discLongBasePrice) / 100000;
		if (lotsLong < 0.0) {
			lotsLong = null;
		} else {
			lotsLong = Math.round(lotsLong * 1000.0) / 1000.0;
		}

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

	public ActionResult setPositionType() {
		accessKey = configService.getAuthKey();
		if (ticketNo == null || selValue == null) return new Redirect("./?ak=" + accessKey);
		positionService.setPositionType(ticketNo, selValue);
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
