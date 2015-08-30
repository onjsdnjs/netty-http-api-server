package com.xjd.web.view;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.support.DelegatingMessageSource;
import org.springframework.context.support.MessageSourceAccessor;

import com.xjd.ct.utl.DateUtil;
import com.xjd.ct.utl.respcode.RespCode;
import com.xjd.ct.utl.respcode.RespCodeAccessor;

public abstract class ViewUtil {
	private static Logger log = LoggerFactory.getLogger(ViewUtil.class);

	protected static final String NONE_CODE = ViewUtil.class.getSimpleName() + ".NONECODE";
	protected static MessageSourceAccessor messageSourceAccessor = new MessageSourceAccessor(new DelegatingMessageSource());

	public static MessageSourceAccessor getMessageSourceAccessor() {
		return messageSourceAccessor;
	}

	public static void setMessageSourceAccessor(MessageSourceAccessor messageSourceAccessor) {
		ViewUtil.messageSourceAccessor = messageSourceAccessor;
	}

	public static View defaultView() {
		return defaultView(RespCode.RESP_0000);
	}

	public static View defaultView(String returnCode) {
		return defaultView(returnCode, null, null, null, null);
	}

	public static View defaultView(String returnCode, String returnMsg) {
		return defaultView(returnCode, null, returnMsg, null, null);
	}

	public static View defaultView(String returnCode, Object[] args) {
		return defaultView(returnCode, args, null, null, null);
	}

	public static View defaultView(String returnCode, Object[] args, String returnMsg) {
		return defaultView(returnCode, args, returnMsg, null, null);
	}

	public static View defaultView(String returnCode, Object[] args, String returnMsg, String originalCode, String originalMsg) {
		returnCode = filterReturnCode(returnCode);
		View view = new View();
		view.setReturnCode(returnCode);
		String msg = null;
		if (returnMsg != null) { // 使用传入的msg
			msg = getMessageSourceAccessor().getMessage(NONE_CODE, args, returnMsg);
		} else { // 查询一下
			msg = RespCodeAccessor.getMessage(returnCode);
			if (msg == null) {
				log.warn("Cannot find 'returnMsg' for 'returnCode'[{}]", returnCode);
			} else {
				msg = getMessageSourceAccessor().getMessage(NONE_CODE, args, msg);
			}
		}
		if (msg == null) {
			msg = "Unknown returnCode[" + returnCode + "]";
		}
		view.setReturnMsg(msg);
		view.setOriginalCode(originalCode);
		view.setOriginalMsg(originalMsg);
		return view;
	}

	public static String filterReturnCode(String returnCode) {
		return returnCode;
	}

	public static ViewBuilder builder() {
		return new ViewBuilder();
	}

	public static ViewBuilder builder(View view) {
		return new ViewBuilder(view);
	}

	public static class ViewBuilder {
		private String returnCode;
		private String returnMsg;
		private Object[] resultArgs;
		private String service;
		private String version;
		private long timestamp;
		private ViewBody body;

		private View appView;

		private String originalCode;
		private String originalMsg;

		public ViewBuilder() {
		}

		public ViewBuilder(View view) {
			this.appView = view;
			if (view != null) {
				this.returnCode = view.getReturnCode();
				this.returnMsg = view.getReturnMsg();
				this.service = view.getService();
				this.version = view.getVersion();
				this.timestamp = view.getTimestamp();
				this.body = view.getBody();
				this.originalCode = view.getOriginalCode();
				this.originalMsg = view.getOriginalMsg();
			}
		}

		public ViewBuilder returnCode(String returnCode) {
			this.returnCode = returnCode;
			return this;
		}

		public ViewBuilder returnMsg(String returnMsg) {
			this.returnMsg = returnMsg;
			return this;
		}

		public ViewBuilder resultArgs(Object[] resultArgs) {
			this.resultArgs = resultArgs;
			return this;
		}

		public ViewBuilder service(String service) {
			this.service = service;
			return this;
		}

		public ViewBuilder version(String version) {
			this.version = version;
			return this;
		}

		public ViewBuilder timestamp(long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public ViewBuilder timestamp(Date timestamp) {
//			this.timestamp = DateUtil.format(timestamp, DateUtil.PATTERN_YEAR2MILLISECOND);
			this.timestamp = timestamp.getTime();
			return this;
		}

		public ViewBuilder body(ViewBody body) {
			this.body = body;
			return this;
		}

		public ViewBuilder originalCode(String originalCode) {
			this.originalCode = originalCode;
			return this;
		}

		public ViewBuilder originalMsg(String originalMsg) {
			this.originalMsg = originalMsg;
			return this;
		}

		public View build() {
			View view = defaultView(returnCode, resultArgs, returnMsg, originalCode, originalMsg);
			view.setService(service);
			view.setTimestamp(timestamp);
			view.setVersion(version);
			view.setBody(body);

			if (appView != null) {
				BeanUtils.copyProperties(view, appView);
				view = appView;
			}

			return view;
		}
	}
}
