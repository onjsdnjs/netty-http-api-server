package com.xjd.netty.core;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.xjd.netty.HttpResponse;
import com.xjd.netty.annotation.RequestMapping;

public class HttpRequestRouter {

	protected ApplicationContext contxt;
	protected Map<String, RequestMapper> requestMap;

	public HttpRequestRouter(ApplicationContext contxt, Map<String, RequestMapper> requestMap) {
		this.contxt = contxt;
		this.requestMap = requestMap;
	}

	public HttpResponse support(NettyHttpRequest request) {
		String uri = request.getUri();
		RequestMapper reqMapper = requestMap.get(uri);
		if (reqMapper == null) {
			NettyHttpResponse res = new NettyHttpResponse();
			res.setStatus(HttpResponseStatus.NOT_FOUND);
			return res;
		}
		RequestMapping.Method spMethod = RequestMapping.Method.valueOfCode(reqMapper.getReqMethod());
		if (spMethod != RequestMapping.Method.ALL && spMethod != RequestMapping.Method.valueOfCode(request.getMethod().name())) {
			NettyHttpResponse res = new NettyHttpResponse();
			res.setStatus(HttpResponseStatus.FORBIDDEN);
			return res;
		}
		if (!reqMapper.isReqSupportMultipart() && request.isMultipart()) {
			NettyHttpResponse res = new NettyHttpResponse();
			res.setStatus(HttpResponseStatus.FORBIDDEN);
			return res;
		}
		return null;
	}

	public HttpResponse route(NettyHttpRequest request) {
		System.out.println(request.getUploadedFiles().size());

		for (FileUpload f : request.getUploadedFiles()) {
			System.out.println(f.getFilename() + ", " + f.getContentType() + ", " + f.length());
		}

		if (request.isCustomBody()) {
			try {
				System.out.println(new String(request.getBody(), "utf8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

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
