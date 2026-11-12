package me.aap.fermata.addon.tv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TvHttpConsentPolicyTest {
	@Test
	public void grantsAreBoundToSourceTypeAndIdentity() {
		String playlist = TvHttpConsentPolicy.sourceKey(
				TvHttpConsentPolicy.SourceType.M3U, "https://example.invalid/list.m3u");
		String otherPlaylist = TvHttpConsentPolicy.sourceKey(
				TvHttpConsentPolicy.SourceType.M3U, "https://example.invalid/other.m3u");
		String xtream = TvHttpConsentPolicy.sourceKey(
				TvHttpConsentPolicy.SourceType.XTREAM, "42");
		String stalker = TvHttpConsentPolicy.sourceKey(
				TvHttpConsentPolicy.SourceType.STALKER, "42");

		assertNotEquals(playlist, otherPlaylist);
		assertNotEquals(xtream, stalker);
		assertFalse(playlist.contains("example.invalid"));
		assertFalse(xtream.contains("42"));
	}

	@Test
	public void playlistIdentityIgnoresHostCaseAndFragment() {
		assertEquals(TvHttpConsentPolicy.sourceKey(TvHttpConsentPolicy.SourceType.M3U,
						"https://EXAMPLE.invalid/list.m3u#one"),
				TvHttpConsentPolicy.sourceKey(TvHttpConsentPolicy.SourceType.M3U,
						"https://example.invalid/list.m3u#two"));
	}

	@Test
	public void httpRequiresSourceGrantButHttpsDoesNot() {
		assertTrue(TvHttpConsentPolicy.requiresConsent("http://portal.invalid/list", false));
		assertFalse(TvHttpConsentPolicy.requiresConsent("http://portal.invalid/list", true));
		assertFalse(TvHttpConsentPolicy.requiresConsent("https://portal.invalid/list", false));
		assertTrue(TvHttpConsentPolicy.requiresConsent("ftp://portal.invalid/list", true));
	}

	@Test
	public void credentialBearingProvidersRequireElevatedWarning() {
		assertEquals(TvHttpConsentPolicy.WarningLevel.STANDARD,
				TvHttpConsentPolicy.warningLevel(TvHttpConsentPolicy.SourceType.M3U));
		assertEquals(TvHttpConsentPolicy.WarningLevel.CREDENTIALS_EXPOSED,
				TvHttpConsentPolicy.warningLevel(TvHttpConsentPolicy.SourceType.XTREAM));
		assertEquals(TvHttpConsentPolicy.WarningLevel.CREDENTIALS_EXPOSED,
				TvHttpConsentPolicy.warningLevel(TvHttpConsentPolicy.SourceType.STALKER));
	}

	@Test
	public void httpsRedirectDowngradeIsBlocked() {
		assertTrue(TvHttpConsentPolicy.blocksRedirect(
				"https://portal.invalid/a", "http://portal.invalid/b"));
		assertFalse(TvHttpConsentPolicy.blocksRedirect(
				"https://portal.invalid/a", "https://portal.invalid/b"));
		assertFalse(TvHttpConsentPolicy.blocksRedirect(
				"http://portal.invalid/a", "http://portal.invalid/b"));
	}
}
