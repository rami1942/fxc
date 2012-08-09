package org.dyndns.bluefield.fxc.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name="configuration")
public class Configuration {

	@Id
	public String confKey;
	public String confValue;
	
	public String getConfKey() {
		return confKey;
	}
	public void setConfKey(String confKey) {
		this.confKey = confKey;
	}
	public String getConfValue() {
		return confValue;
	}
	public void setConfValue(String confValue) {
		this.confValue = confValue;
	}	
}
