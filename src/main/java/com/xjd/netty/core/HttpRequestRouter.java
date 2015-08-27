package com.xjd.netty.core;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;

import com.xjd.netty.HttpResponse;

public class HttpRequestRouter {

	public HttpResponse support(NettyHttpRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpResponse route(NettyHttpRequest request) {
		// TODO Auto-generated method stub
		NettyHttpResponse res = new NettyHttpResponse();
		res.setStatus(HttpResponseStatus.OK);
		HttpHeaders headers = new DefaultHttpHeaders();
		headers.set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
		res.setHeaders(headers);
		res.setContent(new File("/tmp/tmp.txt"));
		return res;
	}

}
