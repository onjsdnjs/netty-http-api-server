package com.xjd.netty.core;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.File;
import java.io.UnsupportedEncodingException;

import com.xjd.netty.HttpResponse;

public class HttpRequestRouter {

	public HttpResponse support(NettyHttpRequest request) {
		// TODO Auto-generated method stub
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
