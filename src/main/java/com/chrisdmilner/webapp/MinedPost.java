package com.chrisdmilner.webapp;

import twitter4j.GeoLocation;
import twitter4j.Place;

import java.util.Date;

public class MinedPost {

	private Date createdTime;
	private GeoLocation location;
	private Place place;
	private String lang;
	private String content;
	private boolean byUser;

	public MinedPost(Date time, GeoLocation loc, Place place, String lang, String content, boolean byUser) {
		this.createdTime = time;
		this.location = loc;
		this.place = place;
		this.lang = lang;
		this.content = content;
		this.byUser = byUser;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public GeoLocation getLocation() {
		return location;
	}

	public Place getPlace() {
		return place;
	}

	public String getLanguage() {
		return lang;
	}

	public String getContent() {
		return content;
	}

	public boolean isByUser() {
		return byUser;
	}

	public String toString() {
		return content;
	}
}