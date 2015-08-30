package com.xjd.ct.app.util;

import java.util.HashMap;
import java.util.Map;

import com.xjd.ct.utl.exception.BusinessException;
import com.xjd.ct.utl.respcode.RespCode;

/**
 * 请求上下文
 * 
 * @author elvis.xu
 * @since 2014-12-1
 */
public class RequestContext {

	private static final String KEY_USER_IP = RequestContext.class.getName() + ".user.ip";
	private static final String KEY_USER_ID = RequestContext.class.getName() + ".user.id";
	private static final String KEY_SERVICE_NAME = RequestContext.class.getName() + ".service.name";
	private static final String KEY_SERVICE_VERSION = RequestContext.class.getName() + ".service.version";
	private static final String KEY_APP_AGENT = RequestContext.class.getName() + ".app.agent";
	private static final String KEY_APP_TYPE = RequestContext.class.getName() + ".app.type";
	private static final String KEY_APP_VERSION = RequestContext.class.getName() + ".app.version";

	protected static ThreadLocal<Map<String, Object>> requestThreadLocal = new ThreadLocal<Map<String, Object>>();

	private RequestContext() {
	}

	public static Map<String, Object> get() {
		Map<String, Object> map = requestThreadLocal.get();
		if (map == null) {
			map = new HashMap<String, Object>();
			requestThreadLocal.set(map);
		}
		return map;
	}

	public static void put(String key, Object val) {
		get().put(key, val);
	}

	public static Object get(String key) {
		return get().get(key);
	}

	public static String getAsString(String key) {
		return (String) get().get(key);
	}

	public static void putUserId(Long userId) {
		put(KEY_USER_ID, userId);
	}

	public static Long getUserId() {
		return (Long) get(KEY_USER_ID);
	}

	public static Long checkAndGetUserId() {
		Long userId = getUserId();
		if (userId == null) {
			throw new BusinessException(RespCode.RESP_0111);
		}
		return userId;
	}

	public static void putUserIp(String userIp) {
		put(KEY_USER_IP, userIp);
	}

	public static String getUserIp() {
		return getAsString(KEY_USER_IP);
	}

	public static void putServiceName(String name) {
		put(KEY_SERVICE_NAME, name);
	}

	public static String getServiceName() {
		return getAsString(KEY_SERVICE_NAME);
	}

	public static void putServiceVersion(String method) {
		put(KEY_SERVICE_VERSION, method);
	}

	public static String getServiceVersion() {
		return getAsString(KEY_SERVICE_VERSION);
	}

	public static void putAppAgent(String agent) {
		put(KEY_APP_AGENT, agent);
	}

	public static String getAppAgent() {
		return getAsString(KEY_APP_AGENT);
	}

	public static void putAppType(Byte type) {
		put(KEY_APP_TYPE, type);
	}

	public static Byte getAppType() {
		return (Byte) get(KEY_APP_TYPE);
	}

	public static void putAppVersion(Short verCode) {
		put(KEY_APP_VERSION, verCode);
	}

	public static Short getAppVersion() {
		return (Short) get(KEY_APP_VERSION);
	}

	public static void clear() {
		requestThreadLocal.remove();
	}

}
