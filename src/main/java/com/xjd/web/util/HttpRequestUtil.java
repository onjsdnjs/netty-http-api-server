package com.xjd.ct.app.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Http请求工具类
 * @author elvis.xu
 * @since 2015-02-16 15:16
 */
public abstract class HttpRequestUtil {

	/**
	 * 获取客户端真实IP(有代理的请况)
	 * @param request
	 * @return
	 */
	public static String getRealRemoteAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}
