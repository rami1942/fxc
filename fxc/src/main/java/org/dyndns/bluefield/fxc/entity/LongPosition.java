package org.dyndns.bluefield.fxc.entity;

import javax.persistence.Entity;

@Entity(name="long_position")
public class LongPosition {
	public Double openPrice;
	public Integer lots;

	public Double getOpenPrice() {
		return openPrice;
	}
	public void setOpenPrice(Double openPrice) {
		this.openPrice = openPrice;
	}
	public Integer getLots() {
		return lots;
	}
	public void setLots(Integer lots) {
		this.lots = lots;
	}
}
