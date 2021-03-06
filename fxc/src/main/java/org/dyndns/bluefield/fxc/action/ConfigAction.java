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
	@RequestParameter
	public Double tpWidth;
	@RequestParameter
	public Double baseOffset;
	@RequestParameter
	public Double discLongBasePrice;

	public ValidationRules validation = new DefaultValidationRules() {
		@Override
		public void initialize() {
			add("lots", new RequiredValidator(), new NumberValidator());
			add("trapWidth", new RequiredValidator(), new NumberValidator());
			add("tpWidth", new RequiredValidator(), new NumberValidator());
			add("baseOffset", new RequiredValidator(), new NumberValidator());
			add("discLongBasePrice", new RequiredValidator(), new NumberValidator());
		}
	};

	public ActionResult index() {
		lots = configService.getLotsByTrap();
		trapWidth = configService.getTrapWidth();
		tpWidth = configService.getTpWidth();
		baseOffset = configService.getBaseOffset();
		discLongBasePrice = configService.getDiscLongBasePrice();
		return new Forward("index.jsp");
	}

	@Validation(rules="validation", errorPage="index.jsp")
	public ActionResult update() {
		configService.setLots(lots);
		configService.setTrapWidth(trapWidth);
		configService.setTpWidth(tpWidth);
		configService.setBaseOffset(baseOffset == null ? 0.0 : baseOffset);
		configService.setDiscLongBasePrice(discLongBasePrice == null ? 0.0 : discLongBasePrice);
		actionContext.getFlashMap().put(
				"notice", "更新しました。");
		return new Redirect("./");
	}
}
