package org.dyndns.bluefield.fxc.service;

import java.util.Date;

import javax.annotation.Resource;

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
		SettlementHistory last = jdbcManager.from(SettlementHistory.class).orderBy("settleDt desc").limit(1).getSingleResult();
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
}
