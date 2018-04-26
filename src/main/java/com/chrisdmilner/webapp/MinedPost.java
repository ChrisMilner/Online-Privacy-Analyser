package com.chrisdmilner.webapp;

import twitter4j.GeoLocation;
import twitter4j.Place;

import java.util.Date;

/*
 * Mined Post
 *
 * Stores information about a single post made by the user e.g. a tweet or Reddit post.
 *
 * */
public class MinedPost {

	private Date createdTime;       // The time it was created.
	private GeoLocation location;   // The location tagged to the post.
	private Place place;            // The place tagged to the post.
	private String lang;            // The language the post is in.
	private String content;         // The contents of the post.
	private String[] mediaURLs;     // An array of the URLs of media attached to the post.
	private boolean byUser;         // Whether the post was made by the user (True) or was shared by them (False).

	public MinedPost(Date time, GeoLocation loc, Place place, String lang, String content, String[] mediaURLs, boolean byUser) {
		this.createdTime = time;
		this.location = loc;
		this.place = place;
		this.lang = lang;
		this.content = content;
		this.mediaURLs = mediaURLs;
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

    public String[] getMediaURLs() {
        return mediaURLs;
    }

    // Converts the post to a human readable version for command line output.
	public String toString() {
		String out = createdTime + " : " + location + " : " + place + " : " + content + " : ";

		for (String url : mediaURLs)
		    out += url;

		return out;
	}
}