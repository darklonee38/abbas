package me.aap.fermata.addon.tv;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/** Source-bound HTTP consent decisions. Grants are never shared between IPTV source types. */
public final class TvHttpConsentPolicy {
	private TvHttpConsentPolicy() {
	}

	public enum SourceType {
		M3U,
		XTREAM,
		STALKER
	}

	public enum WarningLevel {
		STANDARD,
		CREDENTIALS_EXPOSED
	}

	public static String sourceKey(SourceType type, String stableIdentity) {
		if ((type == null) || (stableIdentity == null) || stableIdentity.isBlank()) {
			throw new IllegalArgumentException("A source type and stable identity are required");
		}
		String identity = stableIdentity.trim();
		if (type == SourceType.M3U) identity = normalizePlaylistIdentity(identity);
		return type.name().toLowerCase(Locale.ROOT) + ':' + sha256(identity);
	}

	public static WarningLevel warningLevel(SourceType type) {
		return (type == SourceType.XTREAM) || (type == SourceType.STALKER) ?
				WarningLevel.CREDENTIALS_EXPOSED : WarningLevel.STANDARD;
	}

	public static boolean requiresConsent(String url, boolean sourceGranted) {
		if (url == null || url.isBlank()) return false;
		String scheme;
		try {
			scheme = URI.create(url.trim()).getScheme();
		} catch (IllegalArgumentException invalid) {
			return true;
		}
		if ("https".equalsIgnoreCase(scheme)) return false;
		if ("http".equalsIgnoreCase(scheme)) return !sourceGranted;
		return true;
	}

	public static boolean blocksRedirect(String currentUrl, String nextUrl) {
		try {
			URI current = URI.create(currentUrl);
			URI next = URI.create(nextUrl);
			return "https".equalsIgnoreCase(current.getScheme()) &&
					"http".equalsIgnoreCase(next.getScheme());
		} catch (RuntimeException invalid) {
			return true;
		}
	}

	private static String normalizePlaylistIdentity(String value) {
		try {
			URI uri = new URI(value);
			String scheme = uri.getScheme();
			if (scheme == null) return value;
			return new URI(scheme.toLowerCase(Locale.ROOT), uri.getRawUserInfo(),
					uri.getHost() == null ? null : uri.getHost().toLowerCase(Locale.ROOT),
					uri.getPort(), uri.getRawPath(), uri.getRawQuery(), null).toString();
		} catch (URISyntaxException invalid) {
			return value;
		}
	}

	private static String sha256(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder result = new StringBuilder(digest.length * 2);
			for (byte b : digest) result.append(String.format(Locale.ROOT, "%02x", b & 0xff));
			return result.toString();
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException("SHA-256 is unavailable", impossible);
		}
	}
}
