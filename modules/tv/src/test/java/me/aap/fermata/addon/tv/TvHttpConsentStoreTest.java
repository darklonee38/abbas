package me.aap.fermata.addon.tv;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import me.aap.utils.pref.BasicPreferenceStore;
import me.aap.utils.pref.PreferenceStore;

public class TvHttpConsentStoreTest {
	@Test
	public void grantsArePersistedPerSourceAndCanBeRevoked() {
		BasicPreferenceStore store = new BasicPreferenceStore();
		String first = "https://one.invalid/list.m3u";
		String second = "https://two.invalid/list.m3u";
		try (PreferenceStore.Edit edit = store.editPreferenceStore()) {
			TvHttpConsentStore.grant(edit, TvHttpConsentPolicy.SourceType.M3U, first);
		}
		assertTrue(TvHttpConsentStore.isGranted(store,
				TvHttpConsentPolicy.SourceType.M3U, first));
		assertFalse(TvHttpConsentStore.isGranted(store,
				TvHttpConsentPolicy.SourceType.M3U, second));
		try (PreferenceStore.Edit edit = store.editPreferenceStore()) {
			TvHttpConsentStore.revoke(edit, TvHttpConsentPolicy.SourceType.M3U, first);
		}
		assertFalse(TvHttpConsentStore.isGranted(store,
				TvHttpConsentPolicy.SourceType.M3U, first));
	}

	@Test
	public void sourceTypesDoNotShareGrants() {
		BasicPreferenceStore store = new BasicPreferenceStore();
		try (PreferenceStore.Edit edit = store.editPreferenceStore()) {
			TvHttpConsentStore.grant(edit, TvHttpConsentPolicy.SourceType.XTREAM, "42");
		}
		assertFalse(TvHttpConsentStore.isGranted(store,
				TvHttpConsentPolicy.SourceType.STALKER, "42"));
	}

	@Test
	public void legacyReviewMarkerIsStoredSeparately() {
		BasicPreferenceStore store = new BasicPreferenceStore();
		assertFalse(TvHttpConsentStore.isLegacyReviewComplete(store));
		try (PreferenceStore.Edit edit = store.editPreferenceStore()) {
			TvHttpConsentStore.markLegacyReviewComplete(edit);
		}
		assertTrue(TvHttpConsentStore.isLegacyReviewComplete(store));
	}
}
