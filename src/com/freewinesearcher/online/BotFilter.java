package com.freewinesearcher.online;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;

public final class BotFilter implements Filter {

	@Override
	public void init(FilterConfig config) throws ServletException {
		//
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws ServletException, IOException {
		Dbutil.logger.info(
				"*********** BOLT FILTER -> Configuration.detectSuspectedBot: " + Configuration.detectSuspectedBot);
		if (Configuration.detectSuspectedBot) {
			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;
			PageHandler p = PageHandler.getInstance(request, response);
			Dbutil.logger.info("*********** BOLT FILTER -> p.cookies: " + p.cookies);
			if (p.cookies != null) {
				for (Cookie c : p.cookies) {
					if (("" + c.getName()).equals("response")) {
						if (("" + c.getValue()).equals(DigestUtils
								.shaHex(((Bottester) request.getSession().getAttribute("bottester")).challenge))) {
							p.botstatus = 0;
						} else {
							// Dbutil.logger.info(DigestUtils.shaHex(((Bottester)request.getSession().getAttribute("bottester")).challenge));
							// Dbutil.logger.info(c.getValue());

						}
						c.setMaxAge(0);
						response.addCookie(c);
					}
				}
			}
			String URL = request.getRequestURL().toString();
			Dbutil.logger.info("*********** BOLT FILTER -> Request URL: " + URL);
			Dbutil.logger.info("*********** BOLT FILTER -> p.getBotstatus(): " + p.getBotstatus());
			if (p.getBotstatus() >= 2 && mustCheck(URL)) {
				Dbutil.logger.info(
						"*********** BOLT FILTER -> calling check.jsp with target URL : request.getRequestURI() : "
								+ request.getRequestURI());
				// request.getSession().setAttribute("originalurl",
				// request.getRequestURL()+(request.getQueryString()!=null?"?"+request.getQueryString():""));
				String newURI = "/check.jsp?targeturl=" + Webroutines.URLEncodeUTF8(request.getRequestURI());
				// response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
				// response.setHeader("Location", newURI);
				// response.sendRedirect(newURI);
				req.getRequestDispatcher(newURI).forward(req, res);
				// chain.doFilter(req, res);
			} else {
				chain.doFilter(req, res);
			}
		} else {
			chain.doFilter(req, res);
		}

	}

	private static boolean mustCheck(String URI) {
		Dbutil.logger.info("*********** BOLT FILTER -> mustCheck -> URI: " + URI);
		if (URI == null) {
			Dbutil.logger.info("*********** BOLT FILTER -> mustCheck -> A : return true ");
			return true;
		}
		if (URI.toLowerCase().startsWith("https://vinopedia.com")
				|| URI.toLowerCase().startsWith("https://www.vinopedia.com")
				|| URI.toLowerCase().startsWith("http://vinopedia.com")
				|| URI.toLowerCase().startsWith("http://www.vinopedia.com")) {
			Dbutil.logger.info("*********** BOLT FILTER -> mustCheck -> B : return false ");
			return false;
		}
		if (URI.toLowerCase().startsWith(
				"https://" + ("DEV".equals(Configuration.serverrole) ? "test" : "www") + ".vinopedia.com/check.jsp?")) {
			Dbutil.logger.info("*********** BOLT FILTER -> mustCheck -> C : return false ");
			return false;
		}
		if (URI.toLowerCase().startsWith(
				"https://" + ("DEV".equals(Configuration.serverrole) ? "test" : "www") + ".vinopedia.com/abuse.jsp")) {
			Dbutil.logger.info("*********** BOLT FILTER -> mustCheck -> D : return false ");
			return false;
		}
		if (URI.toLowerCase().startsWith(
				"https://" + ("DEV".equals(Configuration.serverrole) ? "test" : "www") + ".vinopedia.com/abuse.jsp")) {
			Dbutil.logger.info("*********** BOLT FILTER -> mustCheck -> E : return false ");
			return false;
		}
		Dbutil.logger.info("*********** BOLT FILTER -> mustCheck -> F : return true ");
		return true;
	}

	@Override
	public void destroy() {
		//
	}
}