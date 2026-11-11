package me.aap.fermata.ui.activity;

import android.content.Context;

import java.io.Closeable;
import java.util.Locale;

import me.aap.utils.log.Log;
import me.aap.utils.voice.TextToSpeech;

/**
 * Local-only spoken feedback for the existing ABBAS voice command pipeline.
 * It never falls back to a network-required voice.
 */
final class VoiceFeedbackController implements Closeable {
	private final Context context;
	private TextToSpeech engine;
	private Locale engineLocale;
	private boolean initializing;
	private boolean closed;
	private String pendingText;

	VoiceFeedbackController(Context context) {
		this.context = context.getApplicationContext();
	}

	void speak(String message, Locale locale) {
		if (closed || message == null || message.isBlank() || locale == null) return;
		pendingText = message;
		if (engine != null && locale.equals(engineLocale)) {
			speakPending();
			return;
		}
		if (initializing) return;
		if (engine != null) {
			engine.close();
			engine = null;
			engineLocale = null;
		}
		initializing = true;
		TextToSpeech.createLocalOnly(context, locale).onSuccess(created -> {
			initializing = false;
			if (closed) {
				created.close();
				return;
			}
			engine = created;
			engineLocale = locale;
			speakPending();
		}).onFailure(error -> {
			initializing = false;
			pendingText = null;
			Log.e(error, "ABBAS local voice feedback is unavailable for ", locale);
		});
	}

	private void speakPending() {
		String message = pendingText;
		pendingText = null;
		if ((engine == null) || (message == null) || message.isBlank()) return;
		engine.speak(message).onFailure(error ->
				Log.e(error, "ABBAS failed to speak local voice feedback"));
	}

	void stop() {
		pendingText = null;
		if (engine != null) engine.stop();
	}

	@Override
	public void close() {
		if (closed) return;
		closed = true;
		pendingText = null;
		if (engine != null) {
			engine.close();
			engine = null;
		}
		engineLocale = null;
	}
}
