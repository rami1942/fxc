package org.dyndns.bluefield.fxc.action;

import java.util.List;

import org.dyndns.bluefield.fxc.entity.Configuration;
import org.dyndns.bluefield.fxc.entity.LongPosition;
import org.dyndns.bluefield.fxc.entity.ShortPosition;
import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.Path;
import org.seasar.cubby.action.Redirect;
import org.seasar.extension.jdbc.JdbcManager;

@ActionClass
@Path("/")
public class IndexAction {
	public JdbcManager jdbcManager;

	public List<ShortPosition> shorts;
	public Integer eachLots;
	public Integer numTraps;
	public Double longAverage;
	public List<LongPosition> longs;
	
	public ActionResult index() {
		shorts = jdbcManager.from(ShortPosition.class).orderBy("openPrice desc").getResultList();
		
		Configuration c = jdbcManager.from(Configuration.class).where("confKey=?", "lots").getSingleResult();
		if (c != null) {
			eachLots = Integer.valueOf(c.confValue);
		}
		
		longs = jdbcManager.from(LongPosition.class).orderBy("openPrice desc").getResultList();
		int l = 0;
		double pr = 0.0;
		for (LongPosition p : longs) {
			l += p.lots;
			pr += p.openPrice * p.lots;
		}
		longAverage = pr / l;
		
		numTraps = l / eachLots;
		
		return new Forward("index.jsp");
	}
	
	public ActionResult extendUp() {
		List<ShortPosition> s = jdbcManager.from(ShortPosition.class).orderBy("openPrice desc").limit(1).getResultList();
		ShortPosition sp = s.get(0);
		Configuration c = jdbcManager.from(Configuration.class).where("confKey=?", "trap_width").getSingleResult();
		Double width = Double.valueOf(c.confValue);
		
		ShortPosition np = new ShortPosition();
		Double d = sp.openPrice + width;
		d = Math.round(d * 1000.0) / 1000.0;
		np.openPrice = d;
		np.isReal = 0;
		jdbcManager.insert(np).execute();
		
		return new Redirect("/");
	}
	
	public ActionResult shortenUp() {
		List<ShortPosition> s = jdbcManager.from(ShortPosition.class).orderBy("openPrice desc").limit(1).getResultList();
		ShortPosition sp = s.get(0);
		
		jdbcManager.delete(sp).execute();
		
		return new Redirect("/");
	}
	
	public ActionResult extendDown() {
		List<ShortPosition> s = jdbcManager.from(ShortPosition.class).orderBy("openPrice asc").limit(1).getResultList();
		ShortPosition sp = s.get(0);
		Configuration c = jdbcManager.from(Configuration.class).where("confKey=?", "trap_width").getSingleResult();
		Double width = Double.valueOf(c.confValue);
		
		ShortPosition np = new ShortPosition();
		Double d = sp.openPrice - width;
		d = Math.round(d * 1000.0) / 1000.0;
		np.openPrice = d;
		np.isReal = 0;
		jdbcManager.insert(np).execute();
		
		return new Redirect("/");
	}
	
	public ActionResult shortenDown() {
		List<ShortPosition> s = jdbcManager.from(ShortPosition.class).orderBy("openPrice asc").limit(1).getResultList();
		ShortPosition sp = s.get(0);
		
		jdbcManager.delete(sp).execute();
		
		return new Redirect("/");
	}

	public ActionResult chart() {
		return new Forward("chart.jsp");
	}
}