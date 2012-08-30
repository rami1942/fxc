package org.dyndns.bluefield.fxc.action;

import javax.annotation.Resource;

import org.dyndns.bluefield.fxc.service.ConfigService;
import org.seasar.cubby.action.ActionClass;
import org.seasar.cubby.action.ActionContext;
import org.seasar.cubby.action.ActionResult;
import org.seasar.cubby.action.Forward;
import org.seasar.cubby.action.Redirect;
import org.seasar.cubby.action.RequestParameter;
import org.seasar.cubby.action.Validation;
import org.seasar.cubby.validator.DefaultValidationRules;
import org.seasar.cubby.validator.ValidationRules;
import org.seasar.cubby.validator.validators.NumberValidator;
import org.seasar.cubby.validator.validators.RequiredValidator;

@ActionClass
public class ConfigAction {
	@Resource
	private ConfigService configService;
	@Resource
	private ActionContext actionContext;
	
	@RequestParameter
	public Integer lots;
	@RequestParameter
	public Double trapWidth;
	
	public ValidationRules validation = new DefaultValidationRules() {
		@Override
		public void initialize() {
			add("lots", new RequiredValidator(), new NumberValidator());
			add("trapWidth", new RequiredValidator(), new NumberValidator());
		}
	};
	
	public ActionResult index() {
		lots = configService.getByInteger("lots");
		trapWidth = configService.getByDouble("trap_width");
		return new Forward("index.jsp");
	}
	
	@Validation(rules="validation", errorPage="index.jsp")
	public ActionResult update() {
		configService.set("lots", lots.toString());
		configService.set("trap_width", trapWidth.toString());
		actionContext.getFlashMap().put(
				"notice", "更新しました。");
		return new Redirect("./");
	}
}
