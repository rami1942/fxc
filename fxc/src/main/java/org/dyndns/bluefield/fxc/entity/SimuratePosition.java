package org.dyndns.bluefield.fxc.entity;

public class SimuratePosition {

	public Double openPrice;
	public Double slPrice;

	public Integer posType;
	public Double lots;

	public Integer proLoss;


	public boolean active;

	public Double getOpenPrice() {
		return openPrice;
	}
	public Double getSlPrice() {
		return slPrice;
	}
	public Double getLots() {
		return lots;
	}
	public boolean isLong() {
		return posType == 0;
	}

	public boolean isActive() {
		return active;
	}

}
