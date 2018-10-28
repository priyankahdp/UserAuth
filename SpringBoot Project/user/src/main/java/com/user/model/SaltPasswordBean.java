package com.user.model;

public class SaltPasswordBean {
	private String password;
	private String saltValue;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSaltValue() {
		return saltValue;
	}

	public void setSaltValue(String saltValue) {
		this.saltValue = saltValue;
	}

}
