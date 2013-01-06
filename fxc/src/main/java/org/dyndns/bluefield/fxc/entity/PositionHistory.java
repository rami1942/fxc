package org.dyndns.bluefield.fxc.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name="position_history")
public class PositionHistory {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Integer ticketNo;
	
	public Integer magicNo;
	
	public Integer posType;
	public Integer posCd;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date openDt;
	public Double lots;
	public String symbol;
	public Double openPrice;
	public Double slPrice;
	public Double tpPrice;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date closeDt;
	public Double closePrice;
	
	public Integer swapPoint;
	public Integer profit;
	
	
	public Integer getTicketNo() {
		return ticketNo;
	}
	public void setTicketNo(Integer ticketNo) {
		this.ticketNo = ticketNo;
	}
	public Integer getMagicNo() {
		return magicNo;
	}
	public void setMagicNo(Integer magicNo) {
		this.magicNo = magicNo;
	}
	public Integer getPosType() {
		return posType;
	}
	public void setPosType(Integer posType) {
		this.posType = posType;
	}
	public Integer getPosCd() {
		return posCd;
	}
	public void setPosCd(Integer posCd) {
		this.posCd = posCd;
	}
	public Date getOpenDt() {
		return openDt;
	}
	public void setOpenDt(Date openDt) {
		this.openDt = openDt;
	}
	public Double getLots() {
		return lots;
	}
	public void setLots(Double lots) {
		this.lots = lots;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Double getOpenPrice() {
		return openPrice;
	}
	public void setOpenPrice(Double openPrice) {
		this.openPrice = openPrice;
	}
	public Double getSlPrice() {
		return slPrice;
	}
	public void setSlPrice(Double slPrice) {
		this.slPrice = slPrice;
	}
	public Double getTpPrice() {
		return tpPrice;
	}
	public void setTpPrice(Double tpPrice) {
		this.tpPrice = tpPrice;
	}
	public Date getCloseDt() {
		return closeDt;
	}
	public void setCloseDt(Date closeDt) {
		this.closeDt = closeDt;
	}
	public Double getClosePrice() {
		return closePrice;
	}
	public void setClosePrice(Double closePrice) {
		this.closePrice = closePrice;
	}
	public Integer getSwapPoint() {
		return swapPoint;
	}
	public void setSwapPoint(Integer swapPoint) {
		this.swapPoint = swapPoint;
	}
	public Integer getProfit() {
		return profit;
	}
	public void setProfit(Integer profit) {
		this.profit = profit;
	}
}
