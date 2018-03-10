package com.chrisdmilner.webapp;

import facebook4j.*;
import facebook4j.conf.ConfigurationBuilder;

import java.net.URL;
import java.util.Calendar;
import java.util.List;

// Contains functions related to Facebook data mining.
public class FacebookMiner {

	// Extracts the available Facebook data for a given user ID.
	public static FactBook mine(String id, String at) {

        System.out.println("\n - STARTING FACEBOOK MINER - \n");

	    FactBook fs = new FactBook();
	    Fact rootFact = new Fact<>("Facebook Account", id, null);
        fs.addFact(rootFact);

        System.out.println("   Connecting to API and retrieving user data");

		FacebookFactory ff = new FacebookFactory(configureF4J(at).build());

		// Get the raw Facebook API user data.
		Facebook fb = ff.getInstance();

		User u = null;
        URL picURL;
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

            System.out.println("   Processing the user's profile data");

            if (at.equals("")) {
                u = fb.getUser(id, reading);

                picURL = fb.getPictureURL(id, PictureSize.large);
            } else {
                u = fb.getMe(reading);

                Reading r = new Reading().fields("images");
                ResponseList<Photo> photos = fb.getPhotos(r);
                List<Image> images;
                String url;
                for (Photo photo : photos) {
                     images = photo.getImages();
                     url = images.get(0).getSource().toString();
                     fs.addFact(new Fact<>("Image URL", url, rootFact));
                }

                picURL = fb.getPictureURL(PictureSize.large);

                // TODO fb.getVideos (logged in or not)

                // TODO fb.getInterests etc.

            }

		} catch (FacebookException e) {
			System.err.println("   ERROR getting facebook profile");
			e.printStackTrace();
			return fs;
		}

		if (u.getLanguages() != null) {
			for (int i = 0; i < u.getLanguages().size(); i++) {
				fs.addFact(new Fact<>("Language", u.getLanguages().get(i).getName(), rootFact));
			}
		}

		// Add the available data to the factbook.
		if (u.getName() != null) 		        fs.addFact(new Fact<>("Name", u.getName(), rootFact));
		if (u.getFirstName() != null) 		    fs.addFact(new Fact<>("First Name", u.getFirstName(), rootFact));
		if (u.getMiddleName() != null) 			fs.addFact(new Fact<>("Middle Name", u.getMiddleName(), rootFact));
		if (u.getLastName() != null) 			fs.addFact(new Fact<>("Last Name", u.getLastName(), rootFact));
		if (u.getUsername() != null) 			fs.addFact(new Fact<>("Username", u.getUsername(), rootFact));
		if (picURL != null)                     fs.addFact(new Fact<>("Image URL", picURL.toString(), rootFact));
		if (u.getAgeRange() != null) {
		    // Get current date.
            Calendar max = Calendar.getInstance();
            Calendar min = Calendar.getInstance();

            // Subtract the ages to get birth dates then add as facts
			if (u.getAgeRange().getMin() != null) {
                max.add(Calendar.YEAR, -u.getAgeRange().getMin());
                Fact minAgeFact = new Fact<>("Min Age", u.getAgeRange().getMin(), rootFact);
                fs.addFact(new Fact<>("Max Birth Date", max.getTime(), minAgeFact));
            }
            if (u.getAgeRange().getMax() != null) {
                min.add(Calendar.YEAR, -(u.getAgeRange().getMax() + 1));
                Fact maxAgeFact = new Fact<>("Max Age", u.getAgeRange().getMax(), rootFact);
                fs.addFact(new Fact<>("Min Birth Date", min.getTime(), maxAgeFact));
            }
		}

		if (u.getBio() != null) 				fs.addFact(new Fact<>("Description", u.getBio(), rootFact));

		if (u.getBirthday() != null) {
            String bday = u.getBirthday();
            if (bday.length() == 4) { // YYYY
                fs.addFact(new Fact<>("Birth Year", Util.parseDate(bday, "yyyy"), rootFact));
            } else { // MM/DD
                String[] parts = bday.split("/");
                fs.addFact(new Fact<>("Birth Month", parts[0], rootFact));
                fs.addFact(new Fact<>("Birth Day", parts[1], rootFact));
                if (parts.length > 2)
                    fs.addFact(new Fact<>("Birth Year", Util.parseDate(parts[2], "yyyy"), rootFact));
            }
		}

        if (u.getEmail() != null) 				fs.addFact(new Fact<>("Email", u.getEmail(), rootFact));
		if (u.getGender() != null) 				fs.addFact(new Fact<>("Gender", u.getGender(), rootFact));
		if (u.getTimezone() != null) 			fs.addFact(new Fact<>("Time Zone", u.getTimezone(), rootFact));
		if (u.getHometown() != null && u.getHometown().getName()!=null)
		                                        fs.addFact(new Fact<>("Home Town", u.getHometown().getName(), rootFact));
		if (u.getLocation() != null && u.getLocation().getName()!=null)
		                                        fs.addFact(new Fact<>("Location", u.getLocation().getName(), rootFact));
		if (u.getLocale() != null)				fs.addFact(new Fact<>("Locale", u.getLocale(), rootFact));
		if (u.getLink() != null)				fs.addFact(new Fact<>("Linked URL", u.getLink().toString(), rootFact));
		if (u.getRelationshipStatus() != null)	fs.addFact(new Fact<>("Relationship Status", u.getRelationshipStatus(), rootFact));
		if (u.getPolitical() != null && !u.getPolitical().equals("None ()"))
												fs.addFact(new Fact<>("Politics", u.getPolitical(), rootFact));
		if (u.getReligion() != null && !u.getReligion().equals("None ()"))
												fs.addFact(new Fact<>("Religion", u.getReligion(), rootFact));
		if (u.getWebsite() != null)				fs.addFact(new Fact<>("Linked URL", u.getWebsite().toString(), rootFact));
		if (u.getInterestedIn() != null && !u.getInterestedIn().isEmpty())
												fs.addFact(new Fact<>("Interest In", u.getInterestedIn(), rootFact));
        if (u.getSignificantOther() != null)    fs.addFact(new Fact<>("Partner", u.getSignificantOther().getName(), rootFact));
        if (u.getCover() != null)               fs.addFact(new Fact<>("Image URL", u.getCover().getSource(), rootFact));

        if (u.getEducation() != null) {
            List<User.Education> educations = u.getEducation();
            for (User.Education edu : educations)
                fs.addFact(new Fact<>("Education", edu, rootFact));
        }

        if (u.getWork() != null) {
            List<User.Work> work = u.getWork();
            for (User.Work w : work)
                fs.addFact(new Fact<>("Work", w, rootFact));
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
		    System.out.println("   Not using the given Access Token");
//		    System.out.println(Util.getConfigParameter(props,"f4j.oauth.accessToken="));
		    cb.setOAuthAccessToken(Util.getConfigParameter(props,"f4j.oauth.accessToken="));
        } else cb.setOAuthAccessToken(at);

		return cb;
	}

//	private static String getURLFromImageID(Facebook fb, String id) throws FacebookException {
//        return fb.getPhoto(id).getLink().toString();
//    }

}