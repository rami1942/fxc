package org.dyndns.bluefield.fxc.action;

import java.util.List;

import org.dyndns.bluefield.fxc.entity.Configuration;
import org.dyndns.bluefield.fxc.entity.LongPosition;
import org.dyndns.bluefield.fxc.entity.ShortPosition;
import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.Path;
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
}