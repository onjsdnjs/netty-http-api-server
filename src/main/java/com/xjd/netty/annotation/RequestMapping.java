package com.xjd.netty.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

	String[] value() default {};

	Method method();

	boolean supportMultipart() default false;

	public static enum Method {
		ALL(""), GET("GET"), POST("POST");

		String code;

		Method(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}

		public boolean validCode(String code) {
			return valueOfCode(code) != null;
		}

		public Method valueOfCode(String code) {
			if (code == null) {
				return ALL;
			}
			for (Method method : Method.values()) {
				if (method.code.equalsIgnoreCase(code)) {
					return method;
				}
			}
			return null;
		}

	}
}
