package com.chrisdmilner.webapp;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// Contains functions related to Twitter data mining.
public class TwitterMiner {

	private final static int MAX_FOLLOWER_COUNT = 10000; 	// The number of followers the user can have before they will not be processed.
	private final static int MAX_FRIEND_COUNT = 10000;		// The number of users the user can be following before they will not be processed.

	// Extracts the available Twitter data for a given user handle.
	public static FactBook mine(String screenName) {

        System.out.println("\n - STARTING TWITTER MINER - \n");

		FactBook fb = new FactBook();
		Fact rootFact = new Fact<>("Twitter Handle", screenName, null);
		fb.addFact(rootFact);

		System.out.println("   Connecting to API and retrieving user data");

		// Extract the raw twitter user data.
        TwitterFactory tf = new TwitterFactory(configureT4J().build());
		Twitter twitter = tf.getInstance();
		User u = null;
		long id = 0;

		try {
			u = twitter.showUser(screenName);
			id = u.getId();
		} catch(TwitterException e) {
			System.err.println("   ERROR retrieving user data:");
			e.printStackTrace();
		}

		System.out.println("   Processing the user's data");

		// Add the available facts from the user profile to the factbook.
		fb.addFact(new Fact<>("Name", u.getScreenName(), rootFact));
		fb.addFact(new Fact<>("Name", u.getName(), rootFact));
		if (!u.isDefaultProfileImage()) fb.addFact(new Fact<>("Image URL", u.getOriginalProfileImageURL(), rootFact));
		if (!u.getDescription().equals("")) fb.addFact(new Fact<>("Description", u.getDescription(), rootFact));

		URLEntity[] urls = u.getDescriptionURLEntities();
		if (urls.length > 0) {
			for (int i = 0; i < urls.length; i++) {
				fb.addFact(new Fact<>("Linked URL", urls[i].getExpandedURL(), rootFact));
			}
		}

		fb.addFact(new Fact<>("Max Birth Date", u.getCreatedAt(), rootFact));
		fb.addFact(new Fact<>("Language", u.getLang(), rootFact));
		fb.addFact(new Fact<>("Location", u.getLocation(), rootFact));
		fb.addFact(new Fact<>("Time Zone", u.getTimeZone(), rootFact));
		fb.addFact(new Fact<>("Linked URL", u.getURLEntity().getExpandedURL(), rootFact));

		System.out.println("   Processing the user's tweets");

		// Get the tweets and add them as facts.
		ArrayList<Status> tweets = getTweets(twitter, id);

		for (int i = 0; i < tweets.size(); i++) {
			fb.addFact(new Fact<MinedPost>("Posted", mineTweet(tweets.get(i)), rootFact));
		}

		System.out.println("   Processing the user's friends");

		// Get mutual friends. The users who are followed by and follow the user.
		ArrayList<Long> mutuals = getMutualFriends(twitter, id, u.getFollowersCount(), u.getFriendsCount());
		ArrayList<User> mutualFriendUsers = new ArrayList<User>();

		// Get the user data objects for each mutual friend and adds them as a fact.
		if (mutuals == null) {
			System.out.println("   Too many friends/followers to search through");
		} else {
			mutualFriendUsers = getUsersFromIDs(twitter, mutuals);

			for (int i = 0; i < mutualFriendUsers.size(); i++) {
				fb.addFact(new Fact<>("Friend", mineUser(mutualFriendUsers.get(i)), rootFact));
			}
		}

        System.out.println("\n - TWITTER MINER FINISHED - \n");

		return fb;
	}

	private static ConfigurationBuilder configureT4J() {
        ConfigurationBuilder cb = new ConfigurationBuilder();

        String props = Util.getAPIConfigFile();

	    cb.setDebugEnabled(Util.getConfigParameter(props, "t4j.debug=").equals("true"));
		cb.setPrettyDebugEnabled(Util.getConfigParameter(props, "t4j.prettyDebug=").equals("true"));
        cb.setOAuthConsumerKey(Util.getConfigParameter(props, "t4j.oauth.consumerKey="));
        cb.setOAuthConsumerSecret(Util.getConfigParameter(props, "t4j.oauth.consumerSecret="));
        cb.setOAuthAccessToken(Util.getConfigParameter(props, "t4j.oauth.accessToken="));
        cb.setOAuthAccessTokenSecret(Util.getConfigParameter(props, "t4j.oauth.accessTokenSecret="));

        return cb;
	}

	// Converts a tweet to a an internal class.
	private static MinedPost mineTweet(Status tweet) {	
		return new MinedPost(tweet.getCreatedAt(), tweet.getGeoLocation(), tweet.getPlace(), tweet.getLang(), tweet.getText()); 
	}

	// Converts a user to a an internal class.
	private static MinedUser mineUser(User u) {
		return new MinedUser(u.getName(), u.getScreenName(), u.getLocation(), u.isVerified());
	}

	// Gets as many tweets by a user as possible.
	private static ArrayList<Status> getTweets(Twitter t, long id) {
		int pageno = 1;
	    ArrayList<Status> statuses = new ArrayList<>();

	    // Page through all of the available tweets and add them to the list.
	    while (true) {
			try {
				int size = statuses.size(); 
				Paging page = new Paging(pageno++, 100);
				statuses.addAll(t.getUserTimeline(id, page));
				if (statuses.size() == size) break;
			} catch(TwitterException e) {
				System.err.println("   ERROR retrieving tweets:");
				e.printStackTrace();
			}
	    }

	    return statuses;
	}

	// Gets a list of mutual friends of a given user.
	private static ArrayList<Long> getMutualFriends(Twitter t, long id, int followerNo, int friendNo) {

		// If the counts exceed either maximum then: do not process the friends. 
		if (followerNo <= MAX_FOLLOWER_COUNT && friendNo <= MAX_FRIEND_COUNT) {
			
			// Get the lists of followers and friends and then take the common IDs.
			ArrayList<Long> mutuals = getFollowers(t, id);
			ArrayList<Long> friends = getFriends(t, id);
			mutuals.retainAll(friends);

			return mutuals;
		}
		return null;
	}

	// Gets a list of a user's follower's ID numbers.
	private static ArrayList<Long> getFollowers(Twitter t, long id) {
		ArrayList<Long> idList = new ArrayList<>();

		IDs ids;
		long[] idNumbers;
		long cursor = -1;

		try {

			// Page through and add all of the followers to the list.
			while (cursor != 0) {
				waitForRateLimit(t, "/followers/ids");
				ids = t.getFollowersIDs(id, cursor);
				idNumbers = ids.getIDs();

				for (int i = 0; i < idNumbers.length; i++) {
					idList.add(idNumbers[i]);
				}

				cursor = ids.getNextCursor();
			}
		} catch (TwitterException e) {
			System.err.println("   ERROR retrieving followers:");
			e.printStackTrace();
		}

		return idList;
	}

	// Gets a list of a user's friend's (people they follow) ID numbers.
	private static ArrayList<Long> getFriends(Twitter t, long id) {
		ArrayList<Long> idList = new ArrayList<>();

		IDs ids;
		long[] idNumbers;
		long cursor = -1;

		try {

			// Page through and add all of the friends to the list.
			while (cursor != 0) {
				waitForRateLimit(t, "/followers/ids");
				ids = t.getFriendsIDs(id, cursor);
				idNumbers = ids.getIDs();

				for (int i = 0; i < idNumbers.length; i++) {
					idList.add(idNumbers[i]);
				}

				cursor = ids.getNextCursor();
			}
		} catch (TwitterException e) {
			System.err.println("   ERROR retrieving friends:");
			e.printStackTrace();
		}

		return idList;
	}

	// Get a list of user objects related to a list of user IDs.
	private static ArrayList<User> getUsersFromIDs(Twitter t, ArrayList<Long> ids) {
		ArrayList<User> users = new ArrayList<>();
		int idsRemaining = ids.size();
		int start = 0;
		int nextSet = 0;

		// Get all of the objects in sets of 100 then add them to the list.
		do {
			nextSet = Math.min(100, idsRemaining);
			long[] idSet = new long[nextSet];
			for (int i = 0; i < nextSet; i++) {
				idSet[i] = ids.get(start + i);
			}
			try {
				waitForRateLimit(t, "/users/lookup");
				users.addAll(t.lookupUsers(idSet));
			} catch (TwitterException e) {
				System.err.println("   ERROR getting mutual friend user data");
				e.printStackTrace();
			}
			start += 100;
			idsRemaining -= 100;
		} while (nextSet == 100);

		return users;
	}

	// Checks if an endpoint is being limited and waits for it, if it is.
	private static void waitForRateLimit(Twitter t, String endpoint) {
		try {
			Map<String,RateLimitStatus> rateLimitsMap = t.getRateLimitStatus();
			if (rateLimitsMap.containsKey(endpoint)) {
				RateLimitStatus rls = rateLimitsMap.get(endpoint);
				if (rls.getRemaining() <= 0) {
					System.out.println("   Waiting " + rls.getSecondsUntilReset() + "s for rate limit");
					TimeUnit.SECONDS.sleep(rls.getSecondsUntilReset() + 1);
					//System.out.println("Waiting finished. " + t.getRateLimitStatus().get(endpoint).getSecondsUntilReset() + "s until next reset");
				}
			} else {
				System.err.println(endpoint + " endpoint is incorrect");
				System.err.println(rateLimitsMap.keySet().toString());
			}
		} catch (Exception e) {
			System.err.println("   ERROR getting rate limit");
			e.printStackTrace();
			System.exit(1);
		}
	}

}