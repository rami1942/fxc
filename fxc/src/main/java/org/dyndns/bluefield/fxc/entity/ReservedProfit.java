package org.dyndns.bluefield.fxc.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name="reserved_profit")
public class ReservedProfit {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer id;

	@Temporal(TemporalType.TIMESTAMP)
	public Date reserveDt;

	public Integer amount;
	public String description;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Date getReserveDt() {
		return reserveDt;
	}
	public void setReserveDt(Date reserveDt) {
		this.reserveDt = reserveDt;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
