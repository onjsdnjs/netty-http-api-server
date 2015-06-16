package com.xjd.netty;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NettyHttpRequest implements HttpRequest {

	protected DecoderResult decoderResult;
	protected HttpVersion protocolVersion;
	protected HttpMethod method;
	protected String uri;
	protected SocketAddress remoteAddress;
	protected HttpHeaders headers;
	protected Map<String, List<String>> parameters;
	protected Set<Cookie> cookies;
	protected boolean multipart;
	protected FileUpload[] uploadedFiles;

	@Override
	public HttpHeaders headers() {
		return headers;
	}

	public DecoderResult getDecoderResult() {
		return decoderResult;
	}

	public void setDecoderResult(DecoderResult decoderResult) {
		this.decoderResult = decoderResult;
	}

	public HttpVersion getProtocolVersion() {
		return protocolVersion;
	}

	public NettyHttpRequest setProtocolVersion(HttpVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
		return this;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public NettyHttpRequest setMethod(HttpMethod method) {
		this.method = method;
		return this;
	}

	public String getUri() {
		return uri;
	}

	public NettyHttpRequest setUri(String uri) {
		this.uri = uri;
		return this;
	}

	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public Map<String, List<String>> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, List<String>> parameters) {
		this.parameters = parameters;
	}

	public Set<Cookie> getCookies() {
		return cookies;
	}

	public void setCookies(Set<Cookie> cookies) {
		this.cookies = cookies;
	}

	public boolean isMultipart() {
		return multipart;
	}

	public void setMultipart(boolean multipart) {
		this.multipart = multipart;
	}

	public FileUpload[] getUploadedFiles() {
		return uploadedFiles;
	}

	public void setUploadedFiles(FileUpload[] uploadedFiles) {
		this.uploadedFiles = uploadedFiles;
	}

}
