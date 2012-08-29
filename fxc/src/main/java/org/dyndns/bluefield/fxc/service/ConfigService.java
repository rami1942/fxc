package org.dyndns.bluefield.fxc.service;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.Configuration;
import org.seasar.extension.jdbc.JdbcManager;

public class ConfigService {
	@Resource
	private JdbcManager jdbcManager;
	
	public Integer getByInteger(String key) {
		Configuration c = jdbcManager.from(Configuration.class).where("confKey=?", key).getSingleResult();
		if (c == null) return null;
		return Integer.valueOf(c.confValue);
	}
	
	public Double getByDouble(String key) {
		Configuration c = jdbcManager.from(Configuration.class).where("confKey=?", key).getSingleResult();
		if (c == null) return null;
		return Double.valueOf(c.confValue);
	}
}
