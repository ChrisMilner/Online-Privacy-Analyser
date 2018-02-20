package com.chrisdmilner.webapp;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Account;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.references.OtherUserReference;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class RedditMiner {

	public static FactBook mine(String name) {

	    System.out.println("\n - STARTING REDDIT MINER - \n");

		FactBook fb = new FactBook();
		Fact rootFact = new Fact<>("Reddit User Name", name, null);
		fb.addFact(rootFact);

		System.out.println("   Connecting to API and retrieving user data");

		UserAgent ua = new UserAgent("desktop", "com.chrisdmilner.analyser","1.0.0","RadioactiveMonkey123");

		RedditClient reddit = OAuthHelper.automatic(new OkHttpNetworkAdapter(ua), getCredentials());
        reddit.setLogHttp(false);

		OtherUserReference user = reddit.user(name);
		Account account = user.about();

		System.out.println("   Processing the user's profile data");

		fb.addFact(new Fact<>("Name", name, rootFact));
		fb.addFact(new Fact<>("Max Birth Date", account.getCreated(), rootFact));

        System.out.println("   Processing the user's comments");
		DefaultPaginator<PublicContribution<?>> commentPaginator = user.history("comments").build();

        HashSet<String> subreddits = new HashSet<>();
        List<PublicContribution<?>> comments = commentPaginator.accumulateMerged(-1);
        MinedPost curr;
        for (PublicContribution<?> comment : comments) {
            curr = new MinedPost(comment.getCreated(), null, null, null, comment.getBody());
            fb.addFact(new Fact<>("Commented", curr, rootFact));
            subreddits.add(comment.getSubreddit());
        }

        System.out.println("   Processing the user's posts");
		DefaultPaginator<PublicContribution<?>> postPaginator = user.history("submitted").build();

        List<PublicContribution<?>> posts = postPaginator.accumulateMerged(-1);
        for (PublicContribution<?> post: posts) {
            curr = new MinedPost(post.getCreated(), null, null, null, post.getBody());
            fb.addFact(new Fact<>("Posted", curr, rootFact));
            subreddits.add(post.getSubreddit());
        }

        System.out.println("   Processing the user's subreddits");

        for (String subreddit : subreddits)
            fb.addFact(new Fact<>("Interest", subreddit, rootFact));

        System.out.println("\n - REDDIT MINER FINISHED - \n");

		return fb;
	}

	private static Credentials getCredentials() {
		UUID uuid = UUID.randomUUID();

		String props = Util.getAPIConfigFile();

		String clientId = Util.getConfigParameter(props, "jraw.clientId=");
		String clientSecret = Util.getConfigParameter(props, "jraw.clientSecret=");

		return Credentials.userless(clientId, clientSecret, uuid);
	}

}