package org.dyndns.bluefield.fxc.action;

import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;

@ActionClass
public class ConfigAction {

	public ActionResult index() {
		return new Forward("index.jsp");
	}
}
