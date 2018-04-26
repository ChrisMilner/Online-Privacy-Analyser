package com.chrisdmilner.webapp;

/*
 * Mined User
 *
 * Stores information on a user related to the user e.g. a friend or family member.
 *
 * */
public class MinedUser {

	private String name;        // The user's name.
	private String screenName;  // The user's unique identifier.
	private String location;    // The user's location.
	private boolean verified;   // Whether that user holds a higher account status e.g.Twitter Verified.

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