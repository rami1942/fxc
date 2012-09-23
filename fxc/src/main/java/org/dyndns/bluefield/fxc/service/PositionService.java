package org.dyndns.bluefield.fxc.service;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.DiscPosition;
import org.dyndns.bluefield.fxc.entity.Position;
import org.dyndns.bluefield.fxc.entity.ShortTrap;
import org.seasar.extension.jdbc.JdbcManager;

public class PositionService {
	public static class LongInfo {
		public Double avg;
		public Integer numTraps;
	}

	public static class LosscutInfo {
		public Double price;
		public Double level;
	}

	@Resource
	private JdbcManager jdbcManager;
	@Resource
	private ConfigService configService;

	public List<ShortTrap> getShortTraps() {
		List<ShortTrap> traps = jdbcManager.from(ShortTrap.class).orderBy("openPrice desc").getResultList();
		for (ShortTrap s : traps) {
			int magic = (int)Math.round(s.openPrice * 100  + 100000);
			Position p = jdbcManager.from(Position.class).where("magicNo=?", magic).getSingleResult();
			s.isReal = (p == null) ? 0 : 1;
		}
		return traps;
	}

	public List<Position> getLongPositions() {
		return jdbcManager.from(Position.class).where("symbol='AUDJPYpro' and posType=0 and magicNo=0 and isWideBody=1").orderBy("openPrice desc").getResultList();
	}

	public List<Position> getFreezeLongs() {
		return jdbcManager.from(Position.class).where("symbol='AUDJPYpro' and posType=0 and magicNo=0 and isWideBody=0").orderBy("openPrice desc").getResultList();
	}

	public List<Position> getHedgeShorts() {
		return jdbcManager.from(Position.class).where("symbol='AUDJPYpro' and posType=1 and magicNo=0").orderBy("openPrice desc").getResultList();
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

	public LongInfo calcTraps(List<Position> longs) {
		LongInfo info = new LongInfo();
		Integer eachLots = configService.getByInteger("lots");

		int l = 0;
		double pr = 0.0;
		for (Position p : longs) {
			l += p.lots * 100000;
			pr += p.openPrice * (p.lots * 100000);
		}
		double longAverage = pr / l;
		longAverage = Math.round(longAverage * 1000.0) / 1000.0;

		info.numTraps = l / eachLots;
		info.avg = longAverage;

		return info;
	}

	public void setToFreeze(String price) {
		Position lp = jdbcManager.from(Position.class).where("symbol='AUDJPYpro' and posType=0 and openPrice=?", price).getSingleResult();
		if (lp == null) return;
		lp.isWideBody = 0;
		jdbcManager.update(lp).execute();
	}

	public void setToUnfreeze(String price) {
		Position lp = jdbcManager.from(Position.class).where("symbol='AUDJPYpro' and posType=0 and openPrice=?", price).getSingleResult();
		if (lp == null) return;
		lp.isWideBody = 1;
		jdbcManager.update(lp).execute();
	}

	private long calcRate(List<Position> pos, Double price) {
		long total = 0L;
		for (Position p : pos) {
			if (p.posType == 0) {
				// long
				if (p.slPrice != null && p.slPrice != 0 && p.slPrice > price) continue;
				total += (price - p.openPrice) * p.lots * 100000;
			} else {
				// short
				total += (p.openPrice - price) * p.lots * 100000;
			}
		}
		return total;
	}

	public LosscutInfo calcLosscutRate() {
		long margin = Math.round(configService.getByDouble("margin"));
		long balance = Math.round(configService.getByDouble("balance"));
		List<Position> pos = jdbcManager.from(Position.class).where("symbol='AUDJPYpro' and magicNo=0").getResultList();

		double p;
		double pMp = 0.0;
		for (p = 100; p > 30.5; p-=0.5) {
			long d = calcRate(pos, p);

			double mp = (double)(balance+d) / (double)margin;
			if (mp < 1.0) break;

			pMp = mp;
		}
		p += 0.5;
		pMp = Math.round(pMp * 10000.0) / 100.0;

		LosscutInfo lc = new LosscutInfo();
		lc.price = p;
		lc.level = pMp;

		return lc;
	}

	public Integer exitProfit() {
		List<ShortTrap> traps = getShortTraps();
		Double exitPrice = traps.get(0).openPrice;
		int total = 0;

		int lotsPerTrap = configService.getByInteger("lots");

		for (ShortTrap t : traps) {
			total += (t.openPrice - exitPrice) * lotsPerTrap;
		}

		List<Position> longs = getLongPositions();
		for (Position p : longs) {
			total += (exitPrice - p.openPrice) * p.lots * 100000;
		}

		return total;
	}

	public int calcOneLinePrice() {
		List<Position> longs = getLongPositions();
		int amount = 0;
		for (Position p : longs) {
			amount += p.lots * 100000;
		}
		Double height = configService.getByDouble("trap_width");
		return (int)Math.round(amount * height);
	}

	static class AbsComparator implements Comparator<DiscPosition> {
		public Double currentPrice;

		public AbsComparator(Double currentPrice) {
			this.currentPrice = currentPrice;
		}

		// 赤の条件:
		//  利益が出ていて、(SLがかかっていない || SLがOPの後ろに指してある)
		boolean isRed(DiscPosition p) {
			if (p.isLong) {
				if (p.openPrice < currentPrice && (p.slPrice == 0.0 || p.slPrice < p.openPrice)) {
					return true;
				} else {
					return false;
				}
			} else {
				if (p.openPrice > currentPrice && (p.slPrice == 0.0 || p.slPrice > p.openPrice)) {
					return true;
				} else {
					return false;
				}
			}
		}

		// まず赤優先。次にcurrentPriceに近いもの。
		public int compare(DiscPosition p0, DiscPosition p1) {
			boolean red0 = isRed(p0);
			boolean red1 = isRed(p1);

			if (red0 == red1) {
				return (Math.abs(currentPrice - p0.openPrice) - Math.abs(currentPrice - p1.openPrice) < 0) ? 0 : 1;
			} else {
				if (red0) {
					return -1;
				} else {
					return 1;
				}
			}
		}
	}

	public List<DiscPosition> discPositions(Double currentPrice) {
		List<Position> discs = jdbcManager.from(Position.class).where("symbol='AUDJPYpro' and (pos_type = 1 and magicNo=0) or isWideBody=0").getResultList();
		List<DiscPosition> result = new ArrayList<DiscPosition>(discs.size());
		for (Position p : discs) {
			DiscPosition dp = new DiscPosition();
			dp.isLong = (p.posType == 0);
			dp.openPrice = p.openPrice;
			dp.slPrice = p.slPrice;
			dp.lots = p.lots;

			result.add(dp);
		}

		Collections.sort(result, new AbsComparator(currentPrice));

		return result;
	}
}
