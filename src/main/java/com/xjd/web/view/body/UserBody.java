package com.xjd.web.view.body;

import com.xjd.web.view.ViewBody;
import com.xjd.web.view.vo.UserVo;

/**
 * @author elvis.xu
 * @since 2015-08-30 23:04
 */
public class UserBody extends ViewBody {
	private UserVo user;

	public UserVo getUser() {
		return user;
	}

	public void setUser(UserVo user) {
		this.user = user;
	}
}
