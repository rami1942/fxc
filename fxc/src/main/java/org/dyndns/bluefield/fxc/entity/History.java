package org.dyndns.bluefield.fxc.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name="history")
public class History {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer id;

	public Integer eventType;

	@Temporal(TemporalType.TIMESTAMP)
	public Date eventDt;
	public Double price;
	public Double lots;
	public Integer ticketNo;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getEventType() {
		return eventType;
	}
	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}
	public Date getEventDt() {
		return eventDt;
	}
	public void setEventDt(Date eventDt) {
		this.eventDt = eventDt;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Double getLots() {
		return lots;
	}
	public void setLots(Double lots) {
		this.lots = lots;
	}
	public Integer getTicketNo() {
		return ticketNo;
	}
	public void setTicketNo(Integer ticketNo) {
		this.ticketNo = ticketNo;
	}
}
