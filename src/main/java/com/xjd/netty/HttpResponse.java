package com.xjd.netty;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Collection;

public interface HttpResponse {

	HttpResponseStatus getStatus();

	HttpHeaders getHeaders();

	Collection<Cookie> getCookies();

	Object getContent();
}
