package com.xjd.web.ctrl;

import org.springframework.stereotype.Controller;

import com.xjd.netty.HttpRequest;
import com.xjd.netty.annotation.RequestBody;
import com.xjd.netty.annotation.RequestMapping;

/**
 * @author elvis.xu
 * @since 2015-08-27 22:43
 */
@Controller
@RequestMapping(value = "/test", method = RequestMapping.Method.POST)
public class TestCtrl {

	public Object test(@RequestBody User user, HttpRequest request) {
		System.out.println(request);
		System.out.println(user);
		return user;
	}

	public static class User {
		private String name;
		private int age;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		@Override
		public String toString() {
			return "User{" + "name='" + name + '\'' + ", age=" + age + '}';
		}
	}
}
