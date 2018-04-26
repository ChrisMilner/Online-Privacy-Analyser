package com.chrisdmilner.webapp;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Account;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.references.OtherUserReference;
import net.dean.jraw.references.SubmissionReference;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/*
 * Reddit Miner
 *
 * Uses the JRAW library to access the user's Reddit data and parse it into facts.
 *
 * */
public class RedditMiner {

    // Extracts the available Reddit data for a given username.
	public static FactBook mine(String name) {

	    System.out.println("\n - STARTING REDDIT MINER - \n");

	    // Create the Factbook and the root fact.
		FactBook fb = new FactBook();
		Fact rootFact = new Fact<>("Reddit Account", name, null);
		fb.addFact(rootFact);

		System.out.println("   Connecting to API and retrieving user data");

		// Initialise the user and Reddit API instance.
		UserAgent ua = new UserAgent("desktop", "com.chrisdmilner.analyser","1.0.0","RadioactiveMonkey123");

		RedditClient reddit = OAuthHelper.automatic(new OkHttpNetworkAdapter(ua), getCredentials());
        reddit.setLogHttp(false);

        // Get the user's account.
		OtherUserReference user = reddit.user(name);
		Account account = user.about();

		System.out.println("   Processing the user's profile data");

		// Extract the profile data into facts.
		fb.addFact(new Fact<>("Name", name, rootFact));
		fb.addFact(new Fact<>("Account Created Date", account.getCreated(), rootFact));

        System.out.println("   Processing the user's comments");
		DefaultPaginator<PublicContribution<?>> commentPaginator = user.history("comments").build();

        HashSet<String> subreddits = new HashSet<>();
        List<PublicContribution<?>> comments = commentPaginator.accumulateMerged(-1);
        MinedPost curr;

        // Process the user's comments into mined posts then facts.
        for (PublicContribution<?> comment : comments) {
            curr = new MinedPost(comment.getCreated(), null, null, null, comment.getBody(), new String[0], true);
            fb.addFact(new Fact<>("Commented", curr, rootFact));
            subreddits.add(comment.getSubreddit());
        }

        System.out.println("   Processing the user's posts");
		DefaultPaginator<PublicContribution<?>> postPaginator = user.history("submitted").build();

		// Process the user's posts into mined posts then facts.
        List<PublicContribution<?>> posts = postPaginator.accumulateMerged(-1);
        for (PublicContribution<?> post : posts) {
            Submission postData = ((SubmissionReference) post.toReference(reddit)).inspect();
            String content = postData.getTitle() + ": " + postData.getSelfText();
            String[] media = {postData.getUrl()};
        	curr = new MinedPost(post.getCreated(), null, null, null, content, media, true);
            fb.addFact(new Fact<>("Posted", curr, rootFact));
            subreddits.add(post.getSubreddit());
        }

        System.out.println("   Processing the user's subreddits");

        // Process the user's subscribed subreddits as interests.
        for (String subreddit : subreddits)
            fb.addFact(new Fact<>("Interest", subreddit, rootFact));

        System.out.println("\n - REDDIT MINER FINISHED - \n");

		return fb;
	}

	// Sets up the JRAW credentials.
	private static Credentials getCredentials() {
		UUID uuid = UUID.randomUUID();

		String props = Util.getAPIConfigFile();

		String clientId = Util.getConfigParameter(props, "jraw.clientId=");
		String clientSecret = Util.getConfigParameter(props, "jraw.clientSecret=");

		return Credentials.userless(clientId, clientSecret, uuid);
	}

}