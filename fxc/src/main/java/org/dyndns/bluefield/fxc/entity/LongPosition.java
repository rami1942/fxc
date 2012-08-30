package org.dyndns.bluefield.fxc.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name="long_position")
public class LongPosition {
	
	@Id
	public Double openPrice;
	public Integer lots;
	public Integer isWideBody;

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
	public Integer getIsWideBody() {
		return isWideBody;
	}
	public void setIsWideBody(Integer isWideBody) {
		this.isWideBody = isWideBody;
	}
}
