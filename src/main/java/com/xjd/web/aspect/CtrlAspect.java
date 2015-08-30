package com.xjd.web.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.xjd.netty.HttpRequest;
import com.xjd.netty.context.RequestHolder;

@Component
@Aspect
@Order(1)
public class CtrlAspect {
	private static final Logger log = LoggerFactory.getLogger(CtrlAspect.class);

	@Around("within(com.xjd.web.ctrlr.*) && @annotation(org.springframework.stereotype.Controller)")
	protected Object aroudAdivce(ProceedingJoinPoint jp) throws Throwable {
		HttpRequest request = RequestHolder.get();

		rt = jp.proceed();

	}

}
