package org.dyndns.bluefield.fxc.service;


import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.LongPosition;
import org.dyndns.bluefield.fxc.entity.ShortPosition;
import org.seasar.extension.jdbc.JdbcManager;

public class PositionService {
	public static class LongInfo {
		public Double avg;
		public Integer numTraps;
	}
	
	@Resource
	private JdbcManager jdbcManager;
	@Resource
	private ConfigService configService;

	public List<ShortPosition> getShortPositions() {
		return jdbcManager.from(ShortPosition.class).orderBy("openPrice desc").getResultList();
	}
	
	public List<LongPosition> getLongPositions() {
		return jdbcManager.from(LongPosition.class).orderBy("openPrice desc").getResultList();
	}
	
	public ShortPosition getMaxShortPosition() {
		List<ShortPosition> s = jdbcManager.from(ShortPosition.class).orderBy("openPrice desc").limit(1).getResultList();
		return s.get(0);
	}
	
	public ShortPosition getMinShortPosition() {
		List<ShortPosition> s = jdbcManager.from(ShortPosition.class).orderBy("openPrice asc").limit(1).getResultList();
		return s.get(0);
	}
	
	public void insert(Double price) {
		ShortPosition np = new ShortPosition();
		np.openPrice = Math.round(price * 1000.0) / 1000.0;
		np.isReal = 0;
		jdbcManager.insert(np).execute();
	}
	
	public void delete(ShortPosition sp) {
		jdbcManager.delete(sp).execute();
	}
	
	public LongInfo calcTraps(List<LongPosition> longs) {
		LongInfo info = new LongInfo();
		Integer eachLots = configService.getByInteger("lots");

		int l = 0;
		double pr = 0.0;
		for (LongPosition p : longs) {
			l += p.lots;
			pr += p.openPrice * p.lots;
		}
		double longAverage = pr / l;
		longAverage = Math.round(longAverage * 1000.0) / 1000.0;

		info.numTraps = l / eachLots;
		info.avg = longAverage;
		
		return info;
	}
}
