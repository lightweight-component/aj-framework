package com.ajaxjs.monitor.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息
 */
@Data
public class UserInfo implements Serializable {
	private static final long serialVersionUID = 5729690130594095773L;
	private String salt;
	private String userName;
	private String password;
	private String userNameToken;

	public UserInfo() {
	}

	public UserInfo(String salt, String userName, String password) {
		this.salt = salt;
		this.userName = userName;
		this.password = password;
	}
}
