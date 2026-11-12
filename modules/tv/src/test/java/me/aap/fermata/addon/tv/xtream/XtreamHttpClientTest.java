package me.aap.fermata.addon.tv.xtream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Test;

public class XtreamHttpClientTest {
	@Test
	public void redirectPolicyRejectsHttpsToHttpDowngrade() throws Exception {
		assertTrue(XtreamHttpClient.isHttpsDowngrade(
				new URL("https://portal.invalid/player_api.php"),
				new URL("http://portal.invalid/player_api.php")));
		assertFalse(XtreamHttpClient.isHttpsDowngrade(
				new URL("https://portal.invalid/player_api.php"),
				new URL("https://portal.invalid/next")));
		assertFalse(XtreamHttpClient.isHttpsDowngrade(
				new URL("http://portal.invalid/player_api.php"),
				new URL("http://portal.invalid/next")));
	}
}
