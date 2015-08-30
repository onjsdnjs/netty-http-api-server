package com.xjd.web.ctrl.v10;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Controller;

import com.xjd.netty.HttpRequest;
import com.xjd.netty.annotation.RequestBody;
import com.xjd.netty.annotation.RequestMapping;
import com.xjd.web.model.Test1;
import com.xjd.web.view.View;

/**
 * @author elvis.xu
 * @since 2015-08-27 22:43
 */
@Controller
@RequestMapping(value = "/api/10")
public class TestCtrl {

	@RequestMapping(value = "/test1")
	public View test(@RequestBody Test1 test1, HttpRequest request) {
		// 参数校验
		if (test1 == null) {
			// 参数校验
		}

		// 业务调用

		// 结果封装

		return test1;
	}

	@RequestMapping(value = "/test2", supportMultipart = true)
	public Object test2(HttpRequest request) {
		try {
			request.getUploadedFiles().get(0).renameTo(new File("/data/workspace/tmp/tmp.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "{\"code\":\"ok\"}";
	}
}
