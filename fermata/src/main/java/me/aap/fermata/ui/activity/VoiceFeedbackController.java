package me.aap.fermata.ui.activity;

import android.content.Context;
import android.widget.Toast;

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
	private Locale pendingLocale;
	private boolean initializing;
	private boolean closed;
	private boolean unavailableNotified;
	private String pendingText;

	VoiceFeedbackController(Context context) {
		this.context = context.getApplicationContext();
	}

	void speak(String message, Locale locale) {
		if (closed || message == null || message.isBlank() || locale == null) return;
		pendingText = message;
		pendingLocale = locale;
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
			if ((pendingLocale != null) && !locale.equals(pendingLocale)) {
				created.close();
				speak(pendingText, pendingLocale);
				return;
			}
			engine = created;
			engineLocale = locale;
			unavailableNotified = false;
			speakPending();
		}).onFailure(error -> {
			initializing = false;
			if ((pendingLocale != null) && !locale.equals(pendingLocale)) {
				String latestText = pendingText;
				Locale latestLocale = pendingLocale;
				pendingText = null;
				pendingLocale = null;
				speak(latestText, latestLocale);
				return;
			}
			pendingText = null;
			pendingLocale = null;
			Log.e(error, "ABBAS local voice feedback is unavailable for ", locale);
			if (!unavailableNotified) {
				Toast.makeText(context, offlineVoiceMessage(locale), Toast.LENGTH_LONG).show();
				unavailableNotified = true;
			}
		});
	}

	private static String offlineVoiceMessage(Locale locale) {
		return switch (locale.getLanguage()) {
			case "tr" -> "Çevrimdışı ses bulunamadı. Android Metin Okuma ayarlarından çevrimdışı Türkçe ses yükleyin.";
			case "vi" -> "Không tìm thấy giọng nói ngoại tuyến. Hãy cài đặt giọng nói ngoại tuyến trong cài đặt Chuyển văn bản thành giọng nói của Android.";
			default -> "No offline voice is installed. Install an offline voice in Android Text-to-speech settings.";
		};
	}

	private void speakPending() {
		String message = pendingText;
		pendingText = null;
		pendingLocale = null;
		if ((engine == null) || (message == null) || message.isBlank()) return;
		engine.speak(message).onFailure(error ->
				Log.e(error, "ABBAS failed to speak local voice feedback"));
	}

	void stop() {
		pendingText = null;
		pendingLocale = null;
		if (engine != null) engine.stop();
	}

	@Override
	public void close() {
		if (closed) return;
		closed = true;
		pendingText = null;
		pendingLocale = null;
		if (engine != null) {
			engine.close();
			engine = null;
		}
		engineLocale = null;
	}
}
