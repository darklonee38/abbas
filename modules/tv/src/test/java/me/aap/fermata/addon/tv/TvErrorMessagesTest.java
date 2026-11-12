package me.aap.fermata.addon.tv;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TvErrorMessagesTest {
	@Test
	public void uiMessageDoesNotExposePlaylistOrXtreamCredentials() {
		String safe = TvErrorMessages.from(new IllegalStateException(
				"failed https://user:password@example.invalid/get.php?username=alice&password=secret"));
		assertNotNull(safe);
		assertFalse(safe.contains("user:password"));
		assertFalse(safe.contains("alice"));
		assertFalse(safe.contains("secret"));
		assertFalse(safe.contains("example.invalid"));
	}

	@Test
	public void uiMessageDoesNotExposeStalkerIdentityOrToken() {
		String safe = TvErrorMessages.from(new IllegalStateException(
				"MAC 00:1A:79:01:02:03 serial=serial-secret device_id=device-secret https://stream.invalid/watch?token=secret"));
		assertFalse(safe.contains("01:02:03"));
		assertFalse(safe.contains("serial-secret"));
		assertFalse(safe.contains("device-secret"));
		assertFalse(safe.contains("token=secret"));
		assertFalse(safe.contains("stream.invalid"));
	}
}
