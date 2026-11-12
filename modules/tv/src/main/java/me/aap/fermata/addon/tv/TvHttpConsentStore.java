package me.aap.fermata.addon.tv;

import me.aap.utils.function.BooleanSupplier;
import me.aap.utils.pref.PreferenceStore;
import me.aap.utils.pref.PreferenceStore.Pref;

/** Persists explicit HTTP grants in the preference store owned by the source repository. */
public final class TvHttpConsentStore {
	private static final Pref<BooleanSupplier> LEGACY_REVIEWED =
			Pref.b("TV_HTTP_LEGACY_REVIEWED", false);

	private TvHttpConsentStore() {
	}

	public static boolean isGranted(PreferenceStore store,
			TvHttpConsentPolicy.SourceType type, String stableIdentity) {
		return store.getBooleanPref(grantPref(type, stableIdentity));
	}

	public static void grant(PreferenceStore.Edit edit,
			TvHttpConsentPolicy.SourceType type, String stableIdentity) {
		edit.setBooleanPref(grantPref(type, stableIdentity), true);
	}

	public static void revoke(PreferenceStore.Edit edit,
			TvHttpConsentPolicy.SourceType type, String stableIdentity) {
		edit.setBooleanPref(grantPref(type, stableIdentity), false);
	}

	public static boolean isLegacyReviewComplete(PreferenceStore store) {
		return store.getBooleanPref(LEGACY_REVIEWED);
	}

	public static void markLegacyReviewComplete(PreferenceStore.Edit edit) {
		edit.setBooleanPref(LEGACY_REVIEWED, true);
	}

	private static Pref<BooleanSupplier> grantPref(
			TvHttpConsentPolicy.SourceType type, String identity) {
		String key = TvHttpConsentPolicy.sourceKey(type, identity);
		return Pref.b("TV_HTTP_GRANT_" + key.replace(':', '_'), false);
	}
}
