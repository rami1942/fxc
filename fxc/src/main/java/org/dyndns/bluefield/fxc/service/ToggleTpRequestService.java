package org.dyndns.bluefield.fxc.service;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.Position;
import org.dyndns.bluefield.fxc.entity.ToggleTpRequest;
import org.seasar.extension.jdbc.JdbcManager;

public class ToggleTpRequestService {
	@Resource
	private JdbcManager jdbcManager;
	@Resource
	private ConfigService configService;
	@Resource
	private PositionService positionService;

	public void toggleTp(Integer ticketNo, Integer flag) {
		ToggleTpRequest req = jdbcManager.from(ToggleTpRequest.class).where("ticketNo=?", ticketNo).getSingleResult();
		if (req != null) {
			jdbcManager.delete(req).execute();
		}

		if (flag == 0) {
			req = new ToggleTpRequest();
			req.ticketNo = ticketNo;
			req.tpPrice = 0.0;
			jdbcManager.insert(req).execute();
		} else {
			Position p = positionService.findByTicketNo(ticketNo);
			if (p != null) {
				double tpWidth = configService.getTpWidth();
				req = new ToggleTpRequest();
				req.ticketNo = ticketNo;
				req.tpPrice = (p.magicNo - 100000.0)/100.0 - tpWidth;
				jdbcManager.insert(req).execute();
			}
		}
	}

}
