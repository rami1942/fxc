package org.dyndns.bluefield.fxc.service;


import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.LongPosition;
import org.dyndns.bluefield.fxc.entity.ShortTrap;
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

	public List<ShortTrap> getShortTraps() {
		return jdbcManager.from(ShortTrap.class).orderBy("openPrice desc").getResultList();
	}
	
	public List<LongPosition> getLongPositions() {
		return jdbcManager.from(LongPosition.class).where("isWideBody=1").orderBy("openPrice desc").getResultList();
	}
	
	public List<LongPosition> getFreezeLongs()	 {
		return jdbcManager.from(LongPosition.class).where("isWideBody=0").orderBy("openPrice desc").getResultList();
	}
	
	public ShortTrap getMaxShortPosition() {
		List<ShortTrap> s = jdbcManager.from(ShortTrap.class).orderBy("openPrice desc").limit(1).getResultList();
		return s.get(0);
	}
	
	public ShortTrap getMinShortPosition() {
		List<ShortTrap> s = jdbcManager.from(ShortTrap.class).orderBy("openPrice asc").limit(1).getResultList();
		return s.get(0);
	}
	
	public void insert(Double price) {
		ShortTrap np = new ShortTrap();
		np.openPrice = Math.round(price * 1000.0) / 1000.0;
		np.isReal = 0;
		jdbcManager.insert(np).execute();
	}
	
	public void delete(ShortTrap sp) {
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
	
	public void setToFreeze(String price) {
		LongPosition lp = jdbcManager.from(LongPosition.class).where("openPrice=?", price).getSingleResult();
		if (lp == null) return;
		lp.isWideBody = 0;
		jdbcManager.update(lp).execute();
	}
	
	public void setToUnfreeze(String price) {
		LongPosition lp = jdbcManager.from(LongPosition.class).where("openPrice=?", price).getSingleResult();
		if (lp == null) return;
		lp.isWideBody = 1;
		jdbcManager.update(lp).execute();
	}

}
