package me.aap.fermata.addon.tv.stalker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.junit.Test;

public class StalkerErrorMapperTest {
	private final StalkerAccount account = new StalkerAccount(1, "Home",
			"https://portal.invalid/stalker_portal", "00:1A:79:01:02:03",
			"serial-secret", "device-secret", null, 30);
	private final StalkerErrorMapper mapper = new StalkerErrorMapper(account, null);

	@Test
	public void networkErrorMessagesNeverExposeStalkerIdentity() {
		assertSafe(mapper.map(new UnknownHostException()));
		assertSafe(mapper.map(new ConnectException()));
		assertSafe(mapper.map(new SocketTimeoutException()));
	}

	@Test
	public void fallbackErrorMessageRedactsPortalIdentityAndUrlSecrets() {
		assertSafe(mapper.map(new IllegalStateException(
				"MAC 00:1A:79:01:02:03 serial-secret device-secret " +
						"https://stream.invalid/live?token=private-token")));
	}

	private void assertSafe(java.io.IOException error) {
		String message = error.getMessage();
		assertTrue(message != null && !message.isBlank());
		assertFalse(message.contains("01:02:03"));
		assertFalse(message.contains("serial-secret"));
		assertFalse(message.contains("device-secret"));
		assertFalse(message.contains("private-token"));
	}
}
