package org.dyndns.bluefield.fxc.service;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.DiscPosition;
import org.dyndns.bluefield.fxc.entity.Position;
import org.dyndns.bluefield.fxc.entity.ShortTrap;
import org.dyndns.bluefield.fxc.entity.SimuratePosition;
import org.seasar.extension.jdbc.JdbcManager;

public class PositionService {
	public static class LongInfo {
		public Double avg;
		public Integer numTraps;
		public Double virtualPriceOffset;
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
		return jdbcManager.from(Position.class).where("posCd=1").orderBy("openPrice desc").getResultList();
	}

	public List<Position> getHedgeShorts() {
		return jdbcManager.from(Position.class).where("symbol='AUDJPYpro' and posType=1 and magicNo=0").orderBy("openPrice desc").getResultList();
	}

	public ShortTrap getMaxShortPosition() {
		List<ShortTrap> s = jdbcManager.from(ShortTrap.class).orderBy("openPrice desc").limit(1).getResultList();
		if (s.size() > 0) return s.get(0);
		return null;
	}

	public Double exitRate() {
		List<ShortTrap> traps = getShortTraps();
		if (traps.size() == 0) return null;
		return traps.get(0).openPrice;
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

	public LongInfo calcTraps() {
		List<Position> longs = getLongPositions();
		return calcTraps(longs);
	}

	public LongInfo calcTraps(List<Position> longs) {
		LongInfo info = new LongInfo();
		Integer eachLots = configService.getLotsByTrap();

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
		info.virtualPriceOffset = Math.round(configService.getVpReserve() / (double)l * 1000.0) / 1000.0;
		return info;
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
		long margin = Math.round(configService.getMargin());
		long balance = Math.round(configService.getBalance());
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
		if (traps.size() == 0) return 0;

		Double exitPrice = traps.get(0).openPrice;
		int total = 0;

		int lotsPerTrap = configService.getLotsByTrap();

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
		Double height = configService.getTrapWidth();
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
		List<Position> discs = jdbcManager.from(Position.class).where("symbol='AUDJPYpro' and magicNo = 0 and posCd in (0,2,3,5,6,7)").getResultList();
		List<DiscPosition> result = new ArrayList<DiscPosition>(discs.size());
		for (Position p : discs) {
			DiscPosition dp = new DiscPosition();
			dp.ticketNo = p.ticketNo;
			dp.isLong = (p.posType == 0);
			dp.openPrice = p.openPrice;
			dp.slPrice = p.slPrice;
			dp.lots = p.lots;
			dp.swapPoint = p.swapPoint;
			dp.posType = p.posCd;
			result.add(dp);
		}

		Collections.sort(result, new AbsComparator(currentPrice));

		return result;
	}

	public void setPositionType(Integer ticketNo, Integer posType) {
		Position pos = jdbcManager.from(Position.class).where("ticketNo=?", ticketNo).getSingleResult();
		if (pos == null) return;

		pos.posCd = posType;
		jdbcManager.update(pos).execute();
	}

	public List<SimuratePosition> filteredPositions(double targetRate) {
		List<Position> pos = jdbcManager.from(Position.class).where("symbol='AUDJPYpro' and magicNo=0").orderBy("openPrice desc").getResultList();

		LinkedList<SimuratePosition> sps = new LinkedList<SimuratePosition>();
		for (Position p : pos) {
			SimuratePosition sp = new SimuratePosition();
			sp.openPrice = p.openPrice;
			sp.slPrice = p.slPrice == 0.0 ? null : p.slPrice;
			sp.posType = p.posType;
			sp.lots = p.lots;

			if (sp.isLong()) {
				if (p.posCd == 1) {
					if (targetRate > sp.openPrice) {
						sp.active = false;
					} else {
						sp.active = true;
						sp.proLoss = (int)Math.round((targetRate - sp.openPrice) * sp.lots * 100000);
					}
				} else {
					if (sp.slPrice != null && targetRate < sp.slPrice) sp.active = false;
					else {
						sp.active = true;
						sp.proLoss = (int)Math.round((targetRate - sp.openPrice) * sp.lots * 100000);
					}
				}
			} else {
				if (sp.slPrice != null && targetRate > sp.slPrice) sp.active = false;
				else {
					sp.active = true;
					sp.proLoss = (int)Math.round((sp.openPrice - targetRate) * sp.lots * 100000);
				}
			}

			sps.add(sp);
		}
		return sps;
	}

	public Integer getMargin(List<SimuratePosition> sps) {
		double longs = 0.0;
		double shorts = 0.0;

		int longMargin = 0;
		int shortMargin = 0;

		for (SimuratePosition p : sps) {
			if (!p.isActive()) continue;
			if (p.isLong()) {
				longs += p.lots;
				longMargin += p.openPrice * p.lots * 100000 * 0.04;
			} else {
				shorts += p.lots;
				shortMargin += p.openPrice * p.lots * 100000 * 0.04;
			}
		}

		if (longs == shorts) {
			return longMargin > shortMargin ? longMargin : shortMargin;
		} else if (longs > shorts) {
			return longMargin;
		} else {
			return shortMargin;
		}
	}

}
