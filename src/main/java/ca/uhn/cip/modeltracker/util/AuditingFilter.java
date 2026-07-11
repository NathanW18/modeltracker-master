package ca.uhn.cip.modeltracker.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AuditingFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(AuditingFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("Initiating auditing filter");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		chain.doFilter(request, response);

		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			logAuditEvent((HttpServletRequest) request, (HttpServletResponse) response);
		}
	}

	@Override
	public void destroy() {
	}

	private void logAuditEvent(HttpServletRequest request, HttpServletResponse response) {
		String method = request.getMethod();
		String resourceName = request.getRequestURI();
		boolean export = request.getParameter("exportData") != null;

		if (!isAuditable(method, resourceName, export)) {
			return;
		}

		logger.info("Audit event: method={}, uri={}, query={}, status={}, remoteAddress={}",
				method,
				resourceName,
				request.getQueryString(),
				response.getStatus(),
				request.getRemoteAddr());
	}

	private boolean isAuditable(String method, String resourceName, boolean export) {
		if (resourceName == null) {
			return false;
		}

		return "POST".equalsIgnoreCase(method)
				|| "DELETE".equalsIgnoreCase(method)
				|| "PUT".equalsIgnoreCase(method)
				|| ("GET".equalsIgnoreCase(method)
						&& (export || resourceName.equalsIgnoreCase("/api/stranalyses")));
	}
}
