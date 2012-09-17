package org.dyndns.bluefield.fxc.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.ReservedProfit;
import org.dyndns.bluefield.fxc.entity.SettlementHistory;
import org.seasar.extension.jdbc.JdbcManager;

public class SettlementService {
	static public class SettleResult {
		public Date lastSettlementDt;
		public Integer balance;
		public Integer profit;
		public Integer swapPoint;
		public Integer numRepeat;

		public Integer balanceDiff;
		public Integer profitDiff;

		public Integer kkwProfit;
	}

	@Resource
	private JdbcManager jdbcManager;

	@Resource
	private ConfigService configService;

	public SettleResult getDiffUntilNow() {
		SettleResult result = new SettleResult();

		// 今回
		result.balance = (int)Math.round(configService.getByDouble("balance"));
		result.profit = jdbcManager.selectBySql(Integer.class, "select sum(profit) + sum(swap_point) from position").getSingleResult();

		result.swapPoint = jdbcManager.selectBySql(Integer.class, "select sum(swap_point) from position").getSingleResult();

		// 前回
		SettlementHistory last = jdbcManager.from(SettlementHistory.class).where("settleType=0").orderBy("settleDt desc").limit(1).getSingleResult();
		if (last == null) {
			result.lastSettlementDt = null;
			result.balanceDiff = 0;
			result.profitDiff = 0;
		} else {
			result.lastSettlementDt = last.settleDt;
			result.balanceDiff = (int)Math.round(result.balance - last.balance);
			result.profitDiff = (int)Math.round(result.profit - last.profit);
		}

		// リピート回数
		result.numRepeat = jdbcManager.selectBySql(Integer.class, "select count(*) from history where event_type=0 and event_dt >= ?", last.settleDt).getSingleResult();

		// くるくるワイドスタートからの利益
		SettlementHistory kkwStart = jdbcManager.from(SettlementHistory.class).where("settleType=5").orderBy("settleDt desc").limit(1).getSingleResult();
		if (kkwStart == null) {
			result.kkwProfit = 0;
		} else {
			result.kkwProfit = (int)Math.round(result.balance - kkwStart.balance);
		}
		return result;
	}

	public void update() {
		SettlementHistory hist = new SettlementHistory();
		hist.settleType = 0;
		hist.settleDt = new Date();
		hist.balance = configService.getByDouble("balance");
		hist.profit = jdbcManager.selectBySql(Double.class, "select sum(profit) + sum(swap_point) from position").getSingleResult();
		jdbcManager.insert(hist).execute();
	}

	public List<ReservedProfit> reservedProfits() {
		return jdbcManager.from(ReservedProfit.class).orderBy("reserveDt").getResultList();
	}

	public void reserve(Integer amount, String desc) {
		ReservedProfit profit = new ReservedProfit();
		profit.amount = amount;
		profit.description = desc;
		profit.reserveDt = new Date();
		jdbcManager.insert(profit).execute();
	}

	public void unReserve(Integer id) {
		ReservedProfit profit = jdbcManager.from(ReservedProfit.class).where("id=?", id).getSingleResult();
		jdbcManager.delete(profit).execute();
	}

}
