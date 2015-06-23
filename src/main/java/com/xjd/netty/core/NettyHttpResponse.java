package com.xjd.netty.core;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Collection;

public class NettyHttpResponse implements com.xjd.netty.HttpResponse {

	protected HttpResponseStatus status;
	protected HttpHeaders headers;
	protected Collection<Cookie> cookies;
	protected Object content;

	public HttpResponseStatus getStatus() {
		return status;
	}

	public void setStatus(HttpResponseStatus status) {
		this.status = status;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public Collection<Cookie> getCookies() {
		return cookies;
	}

	public void setCookies(Collection<Cookie> cookies) {
		this.cookies = cookies;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

}
