package me.aap.fermata.addon.tv;

import me.aap.fermata.diagnostics.DiagnosticSanitizer;

/** Converts provider failures into safe, user-visible text without exposing IPTV credentials. */
final class TvErrorMessages {
	private static final DiagnosticSanitizer SANITIZER = new DiagnosticSanitizer();

	private TvErrorMessages() {
	}

	static String from(Throwable error) {
		if (error == null) return "TV source operation failed";
		String message = error.getLocalizedMessage();
		if ((message == null) || message.isBlank()) message = error.toString();
		String safe = SANITIZER.sanitize(message);
		return ((safe == null) || safe.isBlank()) ? "TV source operation failed" : safe;
	}
}
