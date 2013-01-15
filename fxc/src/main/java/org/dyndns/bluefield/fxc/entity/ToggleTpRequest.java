package org.dyndns.bluefield.fxc.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name="toggle_tp_request")
public class ToggleTpRequest {

	@Id
	public Integer ticketNo;
	public Double tpPrice;

	public Integer getTicketNo() {
		return ticketNo;
	}
	public void setTicketNo(Integer ticketNo) {
		this.ticketNo = ticketNo;
	}
	public Double getTpPrice() {
		return tpPrice;
	}
	public void setTpPrice(Double tpPrice) {
		this.tpPrice = tpPrice;
	}


}
