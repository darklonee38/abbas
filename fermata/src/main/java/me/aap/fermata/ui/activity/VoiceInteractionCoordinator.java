package me.aap.fermata.ui.activity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static me.aap.utils.async.Completed.completedVoid;
import static me.aap.utils.async.Completed.failed;
import static me.aap.utils.function.ResultConsumer.Cancel.isCancellation;
import static me.aap.utils.ui.UiUtils.showAlert;

import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.aap.fermata.R;
import me.aap.fermata.addon.AddonInfo;
import me.aap.fermata.addon.AddonManager;
import me.aap.fermata.addon.FermataAddon;
import me.aap.fermata.addon.VoiceSearchAddon;
import me.aap.fermata.media.lib.MediaLib.PlayableItem;
import me.aap.fermata.ui.fragment.MainActivityFragment;
import me.aap.fermata.ui.voice.VoiceIntent;
import me.aap.fermata.ui.voice.VoiceIntentParser;
import me.aap.fermata.ui.voice.VoiceSession;
import me.aap.fermata.ui.voice.VoiceCommandOutcome;
import me.aap.fermata.ui.voice.VoiceInteractionTransaction;
import me.aap.utils.async.FutureSupplier;
import me.aap.utils.async.Promise;
import me.aap.utils.log.Log;
import me.aap.utils.ui.fragment.ActivityFragment;

/** Owns voice recognition, command routing and short-lived selection state for one UI host. */
final class VoiceInteractionCoordinator {
	private final MainActivityDelegate activity;
	private final VoiceSession voiceSession = new VoiceSession();
	private final VoiceInteractionTransaction transaction = new VoiceInteractionTransaction();
	private final VoiceFeedbackController voiceFeedback;
	private VoiceRecognitionSession speechListener;
	private VoiceCommandHandler commandHandler;
	private long nextTransactionId;
	private me.aap.utils.function.Cancellable transactionTimeout =
			me.aap.utils.function.Cancellable.CANCELED;

	VoiceInteractionCoordinator(MainActivityDelegate activity) {
		this.activity = activity;
		this.voiceFeedback = new VoiceFeedbackController(activity.getContext());
	}

	void startContextualAssistant() {
		transaction.cancel();
		transactionTimeout.cancel();
		ActivityFragment fragment = activity.getActiveFragment();
		if (!(fragment instanceof MainActivityFragment) ||
				!((MainActivityFragment) fragment).startVoiceAssistant()) {
			voiceSearch(activity.getCurrentFocus(), false);
		}
	}

	void startGlobalVoiceControl() {
		if (!transaction.isClarifying()) beginTransaction();
		voiceSearch(null, true, transaction.getGeneration());
	}

	boolean handleVoiceSearch(String query) {
		if (!activity.getPrefs().getVoiceControlEnabledPref()) return false;
		Locale locale = Locale.forLanguageTag(activity.getPrefs().getVoiceControlLang(activity));
		String command = VoiceIntentParser.mediaSearchCommand(query, locale);
		if (command == null) return false;
		VoiceIntent intent = VoiceIntentParser.parse(command, locale);
		if ((intent == null) || !canRouteVoiceIntent(intent)) return false;
		activity.post(() -> {
			VoiceCommandHandler handler = getCommandHandler();
			if (!handler.handle(command)) Log.w("Failed to handle media voice search: ", query);
		});
		return true;
	}

	void beginSelection(List<PlayableItem> items) {
		beginSelection(transaction.isActive() ? transaction.getGeneration() : 0L, items);
	}

	void beginSelection(long requestId, List<PlayableItem> items) {
		List<VoiceSession.Option> options = new ArrayList<>(Math.min(3, items.size()));
		for (PlayableItem item : items) {
			if (item == null) continue;
			options.add(new VoiceSession.Option(item.getId(), item.getName(), null));
			if (options.size() == 3) break;
		}
		beginSelectionOptions(requestId, options);
	}

	void beginSelectionOptions(List<VoiceSession.Option> options) {
		beginSelectionOptions(transaction.isActive() ? transaction.getGeneration() : 0L, options);
	}

	void beginSelectionOptions(long requestId, List<VoiceSession.Option> options) {
		if ((options == null) || options.isEmpty()) {
			if (transaction.isCurrent(requestId)) cancelTransaction(requestId);
			else voiceSession.clear();
			return;
		}
		String resultTarget = null;
		resultTarget = options.get(0).getVoiceTarget();
		if (transaction.isActive() && !transaction.beginClarification(requestId, resultTarget)) return;
		voiceSession.beginSelection(options, System.currentTimeMillis());
		if (!transaction.isActive()) return;
		if ((options != null) && (options.size() == 1)) {
			activity.post(() -> {
				if (transaction.isCurrent(requestId)) {
					transaction.beginExecuting(requestId);
					resolveSelection("1");
					transaction.complete(requestId);
					transactionTimeout.cancel();
				}
			});
		} else {
			transactionTimeout.cancel();
			transactionTimeout = activity.postDelayed(() -> cancelTransaction(requestId),
					VoiceSession.SELECTION_TIMEOUT_MS);
			activity.post(() -> {
				if (transaction.isCurrent(requestId) && transaction.isClarifying()) {
					voiceSearch(null, true, requestId);
				}
			});
		}
	}

	void clearSelection() {
		voiceSession.clear();
	}

	void beginTextInput() {
		voiceSession.beginTextInput();
	}

	void beginCommand() {
		voiceSession.beginCommand();
	}

	boolean resolveSelection(String phrase) {
		long now = System.currentTimeMillis();
		if (!voiceSession.isSelectionActive(now)) {
			voiceSession.clear();
			return false;
		}
		VoiceIntent intent = VoiceIntentParser.parse(phrase, activity.getPrefs().getLocalePref());
		if ((intent == null) || (intent.getKind() != VoiceIntent.Kind.SELECTION)) {
			voiceSession.clear();
			return false;
		}

		VoiceSession.Option option = voiceSession.resolveSelection(phrase,
				activity.getPrefs().getLocalePref(), now);
		if (option == null) return true;
		long requestId = transaction.isActive() ? transaction.getGeneration() : 0L;
		String target = option.getVoiceTarget();
		if (target != null) {
			if (!resolveSelection(target, option.getStableId(),
					VoiceReadinessPolicy.deadline(android.os.SystemClock.uptimeMillis()), requestId)) {
				Log.e("Failed to route voice selection to addon ", target);
			}
			return true;
		}
		for (FermataAddon addon : AddonManager.get().getAddons()) {
			if ((addon instanceof VoiceSearchAddon voice) &&
					voice.resolveVoiceSelection(activity, option.getStableId())) return true;
		}
		var selection = activity.getMediaServiceBinder().captureUserSelection();
		activity.getLib().getItem(option.getStableId()).main(activity.getHandler()).onSuccess(item -> {
			// Selection completion is allowed after its own transaction completes, but never
			// after a newer request or explicit cancellation supersedes it.
			if ((requestId != 0L) && !transaction.isCurrentOrCompleted(requestId)) return;
			if (item instanceof PlayableItem playable) {
				activity.getBody().playSelection(selection, playable, -1);
				if (!selection.forwardToCar()) activity.goToItem(playable);
			}
		});
		return true;
	}

	FutureSupplier<List<String>> startSpeechRecognizer() {
		return startSpeechRecognizer(null, false, false);
	}

	FutureSupplier<List<String>> startSpeechRecognizer(@Nullable String locale, boolean textInput) {
		return startSpeechRecognizer(locale, textInput, false);
	}

	boolean isCurrentSession(VoiceRecognitionSession session) {
		return speechListener == session;
	}

	void clearSession(VoiceRecognitionSession session) {
		if (speechListener == session) speechListener = null;
	}

	void onHostPaused() {
		voiceFeedback.stop();
		if (speechListener != null) speechListener.destroy();
		transaction.cancel();
		transactionTimeout.cancel();
		voiceSession.clear();
	}

	void close() {
		voiceFeedback.close();
		if (speechListener != null) speechListener.destroy();
		voiceSession.clear();
		transaction.cancel();
		transactionTimeout.cancel();
	}

	void updateWordSubst() {
		if (commandHandler != null) commandHandler.updateWordSubst();
	}

	private boolean canRouteVoiceIntent(VoiceIntent intent) {
		if (intent.getKind() == VoiceIntent.Kind.PLAYBACK) return true;
		if (intent.getKind() == VoiceIntent.Kind.SELECTION) {
			return voiceSession.isSelectionActive(System.currentTimeMillis());
		}
		String target = intent.getAddon();
		if (target != null) return AddonManager.get().findVoiceAddonInfo(target) != null;
		MainActivityFragment fragment = activity.getActiveMainActivityFragment();
		return (fragment != null) && fragment.isVoiceCommandsSupported();
	}

	private boolean resolveSelection(String target, String stableId, long deadline, long requestId) {
		// Addon activation can take several UI turns. Ignore retries superseded by a newer command.
		if ((requestId != 0L) && !transaction.isCurrentOrCompleted(requestId)) return true;
		AddonManager manager = AddonManager.get();
		AddonInfo info = manager.getVoiceAddonInfo(target);
		if (info == null) return false;

		FermataAddon addon = manager.getAddon(info.className);
		ActivityFragment active = activity.getActiveFragment();
		boolean activeTarget = (active != null) && (active.getFragmentId() == info.addonId);
		if (activeTarget && (addon instanceof VoiceSearchAddon voice) &&
				voice.resolveVoiceSelection(activity, stableId)) return true;
		boolean alive = !activity.getAppActivity().isDestroyed() &&
				!activity.getAppActivity().isFinishing();
		if (!VoiceReadinessPolicy.shouldRetry(android.os.SystemClock.uptimeMillis(),
				deadline, alive)) return false;
		if (!activeTarget && !activity.showFragmentWhenReady(info.addonId)) return false;
		activity.postDelayed(() -> {
			if ((requestId != 0L) && !transaction.isCurrentOrCompleted(requestId)) return;
			if (!resolveSelection(target, stableId, deadline, requestId) &&
					(android.os.SystemClock.uptimeMillis() >= deadline)) {
				Log.e("Failed to resolve voice selection for addon ", target);
			}
		}, VoiceReadinessPolicy.RETRY_DELAY_MS);
		return true;
	}

	boolean isCurrentTransaction(long requestId) {
		return transaction.isCurrent(requestId);
	}

	void completeTransaction(long requestId) {
		transaction.complete(requestId);
		transactionTimeout.cancel();
	}

	private void voiceSearch(@Nullable View focus, boolean global) {
		voiceSearch(focus, global, 0L);
	}

	private void voiceSearch(@Nullable View focus, boolean global, long requestId) {
		boolean textInput = !global && ((focus instanceof EditText) ||
				activity.getAppActivity().isInputActive());
		if (textInput) voiceSession.beginTextInput();
		else if (voiceSession.getMode() != VoiceSession.Mode.SELECTION) voiceSession.beginCommand();
		boolean adaptiveAllowed = !textInput && (voiceSession.getMode() == VoiceSession.Mode.COMMAND);
		startSpeechRecognizer(null, textInput, adaptiveAllowed).onSuccess(results -> {
			// A newer global interaction may have superseded this recognizer while it was listening.
			// Discard its transcript before it can route a command or speak stale feedback.
			if ((requestId != 0L) && !transaction.isCurrent(requestId)) return;
			if ((results == null) || results.isEmpty()) {
				if (!textInput) {
					Locale locale = Locale.forLanguageTag(activity.getPrefs().getVoiceControlLang(activity));
					voiceFeedback.speak(buildRecognitionFailureFeedback(locale), locale);
				}
				return;
			}
			if (focus instanceof EditText && !global) {
				((EditText) focus).setText(results.get(0));
				focus.requestFocus();
			} else if (!global && activity.getAppActivity().isInputActive()) {
				activity.getAppActivity().setTextInput(results.get(0));
			} else {
				VoiceIntent intent = VoiceIntentParser.parse(results.get(0),
						Locale.forLanguageTag(activity.getPrefs().getVoiceControlLang(activity)));
				VoiceCommandOutcome outcome = getCommandHandler().handleWithOutcome(results, requestId);
				Locale feedbackLocale = Locale.forLanguageTag(
						activity.getPrefs().getVoiceControlLang(activity));
				voiceFeedback.speak(outcome.isHandled() ? buildVoiceFeedback(intent, feedbackLocale) :
						buildUnsupportedFeedback(feedbackLocale), feedbackLocale);
				VoiceRecognitionSession current = speechListener;
				if (current != null) current.applyCommandOutcome(outcome);
				if (requestId != 0L) {
					if ((intent != null) && (intent.getKind() == VoiceIntent.Kind.SELECTION) &&
							outcome.isHandled()) {
						transaction.beginExecuting(requestId);
						transaction.complete(requestId);
						transactionTimeout.cancel();
					} else {
						transaction.onOutcome(requestId, intent, outcome);
					}
					if (!transaction.isActive()) transactionTimeout.cancel();
				}
			}
		}).onCompletion((result, fail) -> {
			if (textInput) voiceSession.beginCommand();
		});
	}

	private FutureSupplier<List<String>> startSpeechRecognizer(@Nullable String locale,
			boolean textInput, boolean adaptiveAllowed) {
		FutureSupplier<int[]> check = SpeechRecognitionSupport.checkRecordAudioPermission(activity);
		return check.then(result -> {
			if (result[0] == PERMISSION_GRANTED) return completedVoid();
			return failed(new IllegalStateException("Audio recording permission is not granted"));
		}).onFailure(error -> {
			if (!isCancellation(error)) Log.e(error, "Failed to request RECORD_AUDIO permission");
			showAlert(activity.getContext(), R.string.err_no_audio_record_perm);
		}).then(ignored -> SpeechRecognitionSupport.requireOnDeviceRecognitionService(activity.getContext()))
				.then(ignored -> {
					if (speechListener != null) speechListener.destroy();
					Promise<List<String>> promise = new Promise<>();
					String language = (locale == null) ?
							activity.getPrefs().getVoiceControlLang(activity) : locale;
					Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
							RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					if ((VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) && (locale == null) &&
							activity.getPrefs().getBooleanPref(MainActivityPrefs.VOICE_CONTROL_AUTO_LANG)) {
						intent.putExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_DETECTION, true);
						intent.putStringArrayListExtra(
								RecognizerIntent.EXTRA_LANGUAGE_DETECTION_ALLOWED_LANGUAGES,
								new ArrayList<>(List.of("en-US", "tr-TR", "vi-VN")));
					}
					speechListener = new VoiceRecognitionSession(activity, promise, textInput,
							adaptiveAllowed, voiceSession.getMode() == VoiceSession.Mode.SELECTION);
					speechListener.start(intent);
					return promise;
				});
	}

	private String buildRecognitionFailureFeedback(Locale locale) {
		return switch (locale.getLanguage()) {
			case "tr" -> "Sizi anlayamadım. Tekrar deneyin.";
			case "vi" -> "Tôi chưa nghe rõ. Vui lòng thử lại.";
			default -> "I didn't catch that. Please try again.";
		};
	}

	private String buildUnsupportedFeedback(Locale locale) {
		return switch (locale.getLanguage()) {
			case "tr" -> "Bu komutu henüz desteklemiyorum.";
			case "vi" -> "Lệnh này hiện chưa được hỗ trợ.";
			default -> "That command is not supported yet.";
		};
	}

	private String buildVoiceFeedback(@Nullable VoiceIntent intent, Locale locale) {
		String language = locale.getLanguage();
		boolean turkish = "tr".equals(language);
		boolean vietnamese = "vi".equals(language);
		if (intent == null) {
			return turkish ? "Komut tamamlandı." :
					vietnamese ? "Đã hoàn thành lệnh." : "Command completed.";
		}
		return switch (intent.getKind()) {
			case SELECTION -> turkish ? "Seçiminiz alındı." :
					vietnamese ? "Đã nhận lựa chọn." : "Selection received.";
			case OPEN_ADDON -> turkish ? "İçerik bölümü açılıyor." :
					vietnamese ? "Đang mở mục nội dung." : "Opening the content section.";
			case ADDON_SEARCH -> {
				String query = intent.getQuery();
				String prefix = switch (intent.getSearchAction()) {
					case PLAY -> turkish ? "Oynatmak için aranıyor: " :
							vietnamese ? "Đang tìm để phát: " : "Searching to play: ";
					case FIND -> turkish ? "Aranıyor: " :
							vietnamese ? "Đang tìm: " : "Searching: ";
					case OPEN -> turkish ? "Açılıyor: " :
							vietnamese ? "Đang mở: " : "Opening: ";
				};
				yield prefix + ((query == null) ? " " : query);
			}
			case PLAYBACK -> switch (intent.getPlaybackAction()) {
				case PLAY -> turkish ? "Oynatılıyor." : vietnamese ? "Đang phát." : "Playing.";
				case PAUSE -> turkish ? "Duraklatıldı." : vietnamese ? "Đã tạm dừng." : "Paused.";
				case STOP -> turkish ? "Durduruldu." : vietnamese ? "Đã dừng." : "Stopped.";
				case NEXT -> turkish ? "Sonraki içeriğe geçiliyor." :
						vietnamese ? "Chuyển sang nội dung tiếp theo." : "Skipping to the next item.";
				case PREVIOUS -> turkish ? "Önceki içeriğe geçiliyor." :
						vietnamese ? "Quay lại nội dung trước." : "Going to the previous item.";
				case BACK -> turkish ? "Geri dönülüyor." :
						vietnamese ? "Đang quay lại." : "Going back.";
				case OPEN_CURRENT -> turkish ? "Şu anki içerik açılıyor." :
						vietnamese ? "Đang mở nội dung hiện tại." : "Opening the current item.";
				case PLAY_FAVORITES -> turkish ? "Favoriler oynatılıyor." :
						vietnamese ? "Đang phát mục yêu thích." : "Playing your favorites.";
			};
		};
	}

	private VoiceCommandHandler getCommandHandler() {
		if (commandHandler == null) commandHandler = new VoiceCommandHandler(activity);
		return commandHandler;
	}

	private void beginTransaction() {
		transaction.cancel();
		transactionTimeout.cancel();
		voiceSession.beginCommand();
		transaction.begin(++nextTransactionId);
		long requestId = transaction.getGeneration();
		transactionTimeout = activity.postDelayed(() -> cancelTransaction(requestId), 45_000L);
	}

	private void cancelTransaction(long requestId) {
		if (!transaction.isCurrent(requestId)) return;
		transaction.cancel();
		voiceSession.clear();
		if (speechListener != null) speechListener.destroy();
	}
}
