package org.dyndns.bluefield.fxc.service;

import java.util.Date;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.History;
import org.seasar.extension.jdbc.JdbcManager;

public class HistoryService {
	@Resource
	private JdbcManager jdbcManager;

	public void insertVirtualPrice(int amount) {
		History h = new History();
		h.eventDt = new Date();
		h.eventType = 7;
		h.price = (double)amount;
		jdbcManager.insert(h).execute();
	}
}
