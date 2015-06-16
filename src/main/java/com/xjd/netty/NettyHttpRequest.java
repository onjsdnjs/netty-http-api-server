package com.xjd.netty;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NettyHttpRequest extends DefaultHttpRequest {

	public NettyHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, boolean validateHeaders) {
		super(httpVersion, method, uri, validateHeaders);
	}

	public NettyHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
		super(httpVersion, method, uri);
	}

	protected SocketAddress remoteAddress;
	protected boolean multipart;
	protected Map<String, List<String>> parameters;
	protected Set<Cookie> cookies;
	protected FileUpload[] uploadedFiles;

	public HttpHeaders headers() {
		return headers();
	}

	public Map<String, List<String>> parameters() {
		return parameters;
	}

	public Set<Cookie> cookies() {
		return cookies;
	}

	public FileUpload[] files() {
		return uploadedFiles;
	}
}
