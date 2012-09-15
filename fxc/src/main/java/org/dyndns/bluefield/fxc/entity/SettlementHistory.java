package org.dyndns.bluefield.fxc.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name="settlement_history")
public class SettlementHistory {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer id;

	public Integer settleType;

	@Temporal(TemporalType.TIMESTAMP)
	public Date settleDt;

	public Double balance;
	public Double profit;


	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getSettleType() {
		return settleType;
	}
	public void setSettleType(Integer settleType) {
		this.settleType = settleType;
	}
	public Date getSettleDt() {
		return settleDt;
	}
	public void setSettleDt(Date settleDt) {
		this.settleDt = settleDt;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
	}
	public Double getProfit() {
		return profit;
	}
	public void setProfit(Double profit) {
		this.profit = profit;
	}
}
