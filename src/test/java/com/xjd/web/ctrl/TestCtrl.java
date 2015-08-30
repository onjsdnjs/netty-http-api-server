package com.xjd.web.ctrl;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Controller;

import com.xjd.netty.HttpRequest;
import com.xjd.netty.annotation.RequestBody;
import com.xjd.netty.annotation.RequestMapping;

/**
 * @author elvis.xu
 * @since 2015-08-27 22:43
 */
@Controller
@RequestMapping(value = "/test")
public class TestCtrl {

	@RequestMapping(value = "/test1")
	public Object test(@RequestBody User user, HttpRequest request) {
		System.out.println(request);
		System.out.println(user);
		return user;
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

	public static class User {
		private String name;
		private Integer age;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		@Override
		public String toString() {
			return "User{" + "name='" + name + '\'' + ", age=" + age + '}';
		}
	}
}
