package org.dyndns.bluefield.fxc.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity(name="short_trap")
public class ShortTrap {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer id;
	public Double openPrice;

	@Transient
	public Integer isReal;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Double getOpenPrice() {
		return openPrice;
	}
	public void setOpenPrice(Double openPrice) {
		this.openPrice = openPrice;
	}
	public Integer getIsReal() {
		return isReal;
	}
	public void setIsReal(Integer isReal) {
		this.isReal = isReal;
	}
}
