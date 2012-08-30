package org.dyndns.bluefield.fxc.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.dyndns.bluefield.fxc.service.ConfigService;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;

public class AuthFilter implements Filter {

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest)request;
		HttpSession session = req.getSession();
		if (session.getAttribute("isAuthed") != null) {
			chain.doFilter(request, response);
			return;
		}

		S2Container container = SingletonS2ContainerFactory.getContainer();
		ConfigService configService = (ConfigService)container.getComponent(ConfigService.class);

		String key = configService.getByString("auth_key");
		String paramKey = req.getParameter("ak");
		if (paramKey == null || !key.equals(paramKey)) {
			// auth error
			HttpServletResponse resp = (HttpServletResponse)response;
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		session.setAttribute("isAuthed", new Integer(1));
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

}
