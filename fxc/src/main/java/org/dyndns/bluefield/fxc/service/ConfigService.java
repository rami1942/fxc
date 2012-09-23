package org.dyndns.bluefield.fxc.service;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.entity.Configuration;
import org.seasar.extension.jdbc.JdbcManager;

public class ConfigService {
	@Resource
	private JdbcManager jdbcManager;

	private Integer getByInteger(String key) {
		Configuration c = jdbcManager.from(Configuration.class).where("confKey=?", key).getSingleResult();
		if (c == null) return null;
		return Integer.valueOf(c.confValue);
	}

	private Double getByDouble(String key) {
		Configuration c = jdbcManager.from(Configuration.class).where("confKey=?", key).getSingleResult();
		if (c == null) return null;
		return Double.valueOf(c.confValue);
	}

	private String getByString(String key) {
		Configuration c = jdbcManager.from(Configuration.class).where("confKey=?", key).getSingleResult();
		if (c == null) return null;
		return c.confValue;
	}

	private void set(String key, String value) {
		Configuration c = jdbcManager.from(Configuration.class).where("confKey=?", key).getSingleResult();
		c.confValue = value;
		jdbcManager.update(c).execute();
	}

	public String getAuthKey() {
		return getByString("auth_key");
	}

	public String getBaseDt() {
		return getByString("base_dt");
	}

	public Integer getLotsByTrap() {
		return getByInteger("lots");
	}

	public Integer getVpReserve() {
		return getByInteger("vp_reserve");
	}

	public Integer getProfitReservation() {
		return getByInteger("profit_reservation");
	}

	public Double getTrapWidth() {
		return getByDouble("trap_width");
	}

	public Double getCurrentPrice() {
		return getByDouble("current_price");
	}

	public Double getBalance() {
		return getByDouble("balance");
	}

	public Double getMargin() {
		return getByDouble("margin");
	}

	public Double getBaseOffset() {
		return getByDouble("base_offset");
	}

	public Double getTpWidth() {
		return getByDouble("tp_width");
	}

	public void setLots(Integer lots) {
		set("lots", lots.toString());
	}

	public void setTrapWidth(Double trapWidth) {
		set("trap_width", trapWidth.toString());
	}

	public void setTpWidth(Double tpWidth) {
		set("tp_width", tpWidth.toString());
	}

	public void setBaseOffset(Double baseOffset) {
		set("base_offset", baseOffset.toString());
	}

	public void setVpReserve(Integer vpReserve) {
		set("vp_reserve", vpReserve.toString());
	}

	public void setBaseDt(String baseDt) {
		set("base_dt", baseDt);
	}

	public void setProfitReservation(Integer profit) {
		set("profit_reservation", profit.toString());
	}
}
