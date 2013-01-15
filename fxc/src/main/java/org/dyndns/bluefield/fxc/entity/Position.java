package org.dyndns.bluefield.fxc.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.dyndns.bluefield.fxc.util.PriceUtil;

@Entity(name="position")
public class Position {

	@Id
	public Integer ticketNo;
	public Integer magicNo;

	public Integer posType;

	public String symbol;
	public Double lots;

	public Double openPrice;
	public Double tpPrice;
	public Double slPrice;
	public Integer swapPoint;
	public Double profit;

	public Integer isWideBody;

	public Integer posCd;

	public boolean isLong() {
		return posType == 0;
	}

	public String getLotsDisp() {
		int l = (int)Math.round(lots * 100000);
		return PriceUtil.separateComma(Integer.toString(l));
	}

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
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Double getLots() {
		return lots;
	}
	public void setLots(Double lots) {
		this.lots = lots;
	}
	public Double getOpenPrice() {
		return openPrice;
	}
	public void setOpenPrice(Double openPrice) {
		this.openPrice = openPrice;
	}
	public Double getTpPrice() {
		return tpPrice;
	}
	public void setTpPrice(Double tpPrice) {
		this.tpPrice = tpPrice;
	}
	public Double getSlPrice() {
		return slPrice;
	}
	public void setSlPrice(Double slPrice) {
		this.slPrice = slPrice;
	}
	public Integer getSwapPoint() {
		return swapPoint;
	}
	public void setSwapPoint(Integer swapPoint) {
		this.swapPoint = swapPoint;
	}
	public Double getProfit() {
		return profit;
	}
	public void setProfit(Double profit) {
		this.profit = profit;
	}
	public Integer getIsWideBody() {
		return isWideBody;
	}
	public void setIsWideBody(Integer isWideBody) {
		this.isWideBody = isWideBody;
	}
	public Integer getPosCd() {
		return posCd;
	}
	public void setPosCd(Integer posCd) {
		this.posCd = posCd;
	}

}
