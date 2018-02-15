package com.chrisdmilner.webapp;

import facebook4j.*;
import facebook4j.conf.ConfigurationBuilder;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

// Contains functions related to Facebook data mining.
public class FacebookMiner {

	// FOR TEST USE
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("ERROR incorrect number of arguments. You must give two arguments: a Facebook id and an access token or ''.");
			System.exit(1);
		}

		FactBook fb = mine(args[0], args[1]);
		System.out.println(fb.toString());
	}

	// Extracts the available Facebook data for a given user ID.
	public static FactBook mine(String id, String at) {

        System.out.println("\n - STARTING FACEBOOK MINER - \n");

	    FactBook fs = new FactBook();

        System.out.println("   Connecting to API and retrieving user data");

		FacebookFactory ff = new FacebookFactory(configureF4J(at).build());

		// Get the raw Facebook API user data.
		Facebook fb = ff.getInstance();

		User u = null;
		try {
            fb.extendTokenExpiration();

            Reading reading = new Reading().fields( "about",        "address",          "age_range",
                                                    "birthday",     "cover",            "education",
                                                    "email",        "first_name",       "gender",
                                                    "hometown",     "interested_in",    "last_name",
                                                    "locale",       "location",         "middle_name",
                                                    "name",         "political",        "relationship_status",
                                                    "religion",     "significant_other","sports",
                                                    "website",      "work");

            if (at.equals("")) {
                u = fb.getUser(id, reading);

            } else {
                u = fb.getMe(reading);

                Reading r = new Reading().fields("images");
                ResponseList<Photo> photos = fb.getPhotos(r);
                List<Image> images;
                for (Photo photo : photos) {
                     images = photo.getImages();
                    fs.addFact(new Fact<>("Image URL", images.get(0).getSource().toString(), "Facebook", "Photos"));
                }

                // TODO fb.getVideos (logged in or not)

                // TODO fb.getInterests etc.
                // TODO fb.getFamily ??

            }

		} catch (FacebookException e) {
			System.err.println("   ERROR getting facebook profile");
			e.printStackTrace();
		}

		System.out.println("   Processing the user's profile data");

		if (u.getLanguages() != null) {
			for (int i = 0; i < u.getLanguages().size(); i++) {
				fs.addFact(new Fact<>("Language", u.getLanguages().get(i).getName(), "Facebook", "UserProfile"));
			}
		}

		// Add the available data to the factbook.
		if (u.getName() != null) 		        fs.addFact(new Fact<>("Name", u.getName(), "Facebook", "UserProfile"));
		if (u.getFirstName() != null) 		    fs.addFact(new Fact<>("First Name", u.getFirstName(), "Facebook", "UserProfile"));
		if (u.getMiddleName() != null) 			fs.addFact(new Fact<>("Middle Name", u.getMiddleName(), "Facebook", "UserProfile"));
		if (u.getLastName() != null) 			fs.addFact(new Fact<>("Last Name", u.getLastName(), "Facebook", "UserProfile"));
		if (u.getUsername() != null) 			fs.addFact(new Fact<>("Username", u.getUsername(), "Facebook", "UserProfile"));
		if (u.getPicture() != null && !u.getPicture().isSilhouette())
		                                        fs.addFact(new Fact<>("Image URL", u.getPicture().getURL().toString(), "Facebook", "UserProfile"));
		if (u.getAgeRange() != null) { 			fs.addFact(new Fact<>("Minimum Age", u.getAgeRange().getMin(), "Facebook", "UserProfile"));
												fs.addFact(new Fact<>("Maximum Age", u.getAgeRange().getMax(), "Facebook", "UserProfile")); }
		if (u.getBio() != null) 				fs.addFact(new Fact<>("Description", u.getBio(), "Facebook", "UserProfile"));
		if (u.getBirthday() != null)			fs.addFact(new Fact<>("Birthday", u.getBirthday(), "Facebook", "UserProfile"));
		if (u.getEmail() != null) 				fs.addFact(new Fact<>("Email", u.getEmail(), "Facebook", "UserProfile"));
		if (u.getGender() != null) 				fs.addFact(new Fact<>("Gender", u.getGender(), "Facebook", "UserProfile"));
		if (u.getTimezone() != null) 			fs.addFact(new Fact<>("Time Zone", u.getTimezone(), "Facebook", "UserProfile"));
		if (u.getHometown() != null && u.getHometown().getName()!=null)
		                                        fs.addFact(new Fact<>("Home Town", u.getHometown().getName(), "Facebook", "UserProfile"));
		if (u.getLocation() != null && u.getLocation().getName()!=null)
		                                        fs.addFact(new Fact<>("Location", u.getLocation().getName(), "Facebook", "UserProfile"));
		if (u.getLocale() != null)				fs.addFact(new Fact<>("Locale", u.getLocale(), "Facebook", "UserProfile"));
		if (u.getLink() != null)				fs.addFact(new Fact<>("Linked URL", u.getLink().toString(), "Facebook", "UserProfile"));
		if (u.getRelationshipStatus() != null)	fs.addFact(new Fact<>("Relationship Status", u.getRelationshipStatus(), "Facebook", "UserProfile"));
		if (u.getPolitical() != null)			fs.addFact(new Fact<>("Politics", u.getPolitical(), "Facebook", "UserProfile"));
		if (u.getReligion() != null)			fs.addFact(new Fact<>("Religion", u.getReligion(), "Facebook", "UserProfile"));
		if (u.getWebsite() != null)				fs.addFact(new Fact<>("Linked URL", u.getWebsite().toString(), "Facebook", "UserProfile"));
		if (u.getInterestedIn() != null)		fs.addFact(new Fact<>("Interest In", u.getInterestedIn(), "Facebook", "UserProfile"));
        if (u.getSignificantOther() != null)    fs.addFact(new Fact<>("Partner", u.getSignificantOther().getName(), "Facebook", "UserProfile"));
        if (u.getCover() != null)               fs.addFact(new Fact<>("Image URL", u.getCover().getSource(), "Facebook", "UserProfile"));

        if (u.getEducation() != null) {
            List<User.Education> educations = u.getEducation();
            for (User.Education edu : educations)
                fs.addFact(new Fact<>("Education", edu, "Facebook", "UserProfile"));
        }

        if (u.getWork() != null) {
            List<User.Work> work = u.getWork();
            for (User.Work w : work)
                fs.addFact(new Fact<>("Work", w, "Facebook", "UserProfile"));
        }

        System.out.println("\n - FACEBOOK MINER FINISHED - \n");

		return fs;
	}

	private static ConfigurationBuilder configureF4J(String at) {
		ConfigurationBuilder cb = new ConfigurationBuilder();

		String props = Util.getAPIConfigFile();

		cb.setDebugEnabled(Util.getConfigParameter(props,"f4j.debug=").equals("true"));
		cb.setPrettyDebugEnabled(Util.getConfigParameter(props,"f4j.prettyDebug=").equals("true"));
		cb.setOAuthAppId(Util.getConfigParameter(props,"f4j.oauth.appId="));
		cb.setOAuthAppSecret(Util.getConfigParameter(props,"f4j.oauth.appSecret="));

		if (at.equals("")) {
		    System.out.println("Not using the given Access Token");
		    System.out.println(Util.getConfigParameter(props,"f4j.oauth.accessToken="));
		    cb.setOAuthAccessToken(Util.getConfigParameter(props,"f4j.oauth.accessToken="));
        } else cb.setOAuthAccessToken(at);

		return cb;
	}

	private static String getURLFromImageID(Facebook fb, String id) throws FacebookException {
        return fb.getPhoto(id).getLink().toString();
    }

}