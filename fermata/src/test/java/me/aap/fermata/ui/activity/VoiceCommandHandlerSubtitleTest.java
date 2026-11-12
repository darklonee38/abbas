package me.aap.fermata.ui.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

public class VoiceCommandHandlerSubtitleTest {
	@Test
	public void emptySubtitleListDoesNotSelectAStream() {
		assertNull(VoiceCommandHandler.nextSubtitleStream(List.of(), null));
	}

	@Test
	public void populatedSubtitleListSelectsAndCyclesStreams() {
		List<String> streams = List.of("Turkish", "English", "German");

		assertEquals("Turkish", VoiceCommandHandler.nextSubtitleStream(streams, null));
		assertEquals("English", VoiceCommandHandler.nextSubtitleStream(streams, "Turkish"));
		assertEquals("Turkish", VoiceCommandHandler.nextSubtitleStream(streams, "German"));
	}
}
