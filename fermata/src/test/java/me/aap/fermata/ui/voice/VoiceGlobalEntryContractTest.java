package me.aap.fermata.ui.voice;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class VoiceGlobalEntryContractTest {
	@Test
	public void sharedMicrophonesUseGlobalEntry()
			throws IOException {
		assertTrue(source("fermata/src/main/java/me/aap/fermata/ui/fragment/ToolBarMediator.java")
				.contains("startGlobalVoiceControl()"));
		assertTrue(source("fermata/src/main/java/me/aap/fermata/ui/fragment/DashboardFragment.java")
				.contains("startGlobalVoiceControl()"));
		assertTrue(source("fermata/src/main/java/me/aap/fermata/ui/fragment/FloatingButtonMediator.java")
				.contains("startGlobalVoiceControl()"));
	}


	@Test
	public void voiceActivationAndFeedbackRemainLocalAndUserInitiated() throws IOException {
		String manifest = source("fermata/src/main/AndroidManifest.xml");
		assertTrue(manifest.contains("android.permission.RECORD_AUDIO"));
		String prefs = source("fermata/src/main/java/me/aap/fermata/ui/activity/MainActivityPrefs.java");
		assertTrue(prefs.contains("return getBooleanPref(VOICE_CONTROl_ENABLED);"));
		String feedback = source("fermata/src/main/java/me/aap/fermata/ui/activity/VoiceFeedbackController.java");
		assertTrue(feedback.contains("TextToSpeech.createLocalOnly(context, locale)"));
		assertTrue(feedback.contains("Toast.makeText(context, offlineVoiceMessage(locale)"));
		String coordinator = source(
				"fermata/src/main/java/me/aap/fermata/ui/activity/VoiceInteractionCoordinator.java");
		assertTrue(coordinator.contains("voiceFeedback.speak("));
		assertTrue(coordinator.contains("voiceFeedback.close()"));
	}

	private static String source(String relative) throws IOException {
		Path root = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		while ((root != null) && !Files.exists(root.resolve("settings.gradle"))) {
			root = root.getParent();
		}
		if (root == null) throw new IOException("Project root not found");
		return new String(Files.readAllBytes(root.resolve(relative)), UTF_8);
	}
}
