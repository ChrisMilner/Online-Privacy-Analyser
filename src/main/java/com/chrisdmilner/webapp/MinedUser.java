package com.chrisdmilner.webapp;

public class MinedUser {

	private String name;
	private String screenName;
	private String location;
	private boolean verified;

	public MinedUser(String name, String screenName, String location, boolean verified) {
		this.name = name;
		this.screenName = screenName;
		this.location = location;
		this.verified = verified;
	}

	public String getName() {
		return name;
	}

	public String getScreenName() {
		return screenName;
	}

	public String getLocation() {
		return location;
	}

	public boolean getVerified() {
		return verified;
	}

	public String toString() {
		return name;
	}

}