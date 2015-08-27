package com.xjd.netty.core;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xjd.netty.HttpRequest;
import com.xjd.netty.HttpResponse;
import com.xjd.netty.annotation.RequestBody;
import com.xjd.netty.annotation.RequestMapping;

public class HttpRequestRouter {

	protected static ObjectMapper objectMapper = new ObjectMapper();

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
		String uri = request.getUri();
		RequestMapper reqMapper = requestMap.get(uri);

		Object rt = null;
		try {
			rt = execute(request, reqMapper);
		} catch (IOException e) {
			NettyHttpResponse res = new NettyHttpResponse();
			res.setStatus(HttpResponseStatus.BAD_REQUEST);
			return res;
		} catch (Throwable e) {
			NettyHttpResponse res = new NettyHttpResponse();
			res.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			return res;
		}

		NettyHttpResponse res = new NettyHttpResponse();
		res.setStatus(HttpResponseStatus.OK);
		HttpHeaders headers = new DefaultHttpHeaders();
		headers.set(HttpHeaders.Names.CONTENT_TYPE, reqMapper.getResContentType() + "; charset="
				+ reqMapper.getResCharset().name());
		res.setHeaders(headers);
		res.setContent(rt);
		return res;
	}

	protected Object execute(NettyHttpRequest request, RequestMapper requestMapper) throws IOException,
			InvocationTargetException, IllegalAccessException {
		Method method = requestMapper.getMethod();

		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] paramAnnos = method.getParameterAnnotations();
		List<Object> paramList = new ArrayList<Object>(paramTypes.length);
		for (int i = 0; i < paramTypes.length; i++) {
			Class<?> paramType = paramTypes[i];
			Object param = null;
			if (paramType.isAssignableFrom(HttpRequest.class)) {
				param = request;
			} else {
				Annotation[] pas = paramAnnos[i];
				boolean hasBodyA = false;
				RequestBody bodyA = null;
				for (Annotation a : pas) {
					if (a.annotationType().equals(RequestBody.class)) {
						hasBodyA = true;
						bodyA = (RequestBody) a;
						break;
					}
				}

				if (hasBodyA) {
					byte[] body = request.getBody();

					if (body == null) {
						param = null;
					} else if (String.class.equals(paramType)) {
						param = (new String(body, Charset.forName("utf8")));
					} else if (paramType.isArray()
							&& (byte.class.equals(paramType.getComponentType()) || Byte.class.equals(paramType
									.getComponentType()))) {
						param = (body);
					} else {
						Object paramObj = objectMapper.readValue(new String(body, Charset.forName("utf8")), paramType);
						param = (paramObj);
					}
				}
			}

			paramList.add(param);
		}

		Object bean = contxt.getBean(requestMapper.getBeanName());

		return method.invoke(bean, paramList.toArray());
	}
}
