package me.aap.fermata.ui.voice;

import androidx.annotation.Nullable;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Small deterministic grammar for common car commands. It deliberately does not use an LLM
 * and returns null when the utterance is ambiguous or outside the supported grammar.
 */
public final class VoiceIntentParser {
	private static final Map<String, String> ADDON_ALIASES = new LinkedHashMap<>();

	static {
		alias("youtube", "youtube", "you tube", "yt", "youtube’da", "youtube'da", "youtube’dan", "youtube'dan", "youtube'u", "youtube’u", "youtube'yu", "youtube’yu");
		alias("tv", "tv", "tv'de", "tv’de", "tv'yi", "tv’yi", "televizyonda", "televizyondan", "iptv", "ıptv", "iptv'de", "iptv’de", "iptv'yi", "iptv’yi", "ıptv'yi", "ıptv’yi",
				"television", "tivi", "truyền hình", "truyen hinh", "xtream", "xtreme", "televizyon");
		alias("radio", "radio", "radio'da", "radio’da", "đài", "dai", "radyo", "radyo'da", "radyo’da", "radyo'yu", "radyo’yu", "radyoyu");
		alias("stremio", "stremio");
		alias("podcast", "podcast", "podcasts", "podcast'te", "podcast’te", "podcastte", "podcast'i", "podcast’i");
		alias("audiobook", "audiobook", "audiobooks", "sách nói", "sach noi", "sesli kitap");
		alias("web", "web", "browser", "trình duyệt", "trinh duyet", "tarayıcı", "tarayici");
		alias("chatgpt", "chatgpt", "chat gpt", "gpt", "assistant", "trợ lý", "tro ly");
	}

	private VoiceIntentParser() {
	}

	private static void alias(String canonical, String... values) {
		for (String value : values) ADDON_ALIASES.put(value, canonical);
	}

	@Nullable
	public static VoiceIntent parse(@Nullable String phrase, @Nullable Locale locale) {
		String text = normalize((phrase != null && locale != null && "tr".equals(locale.getLanguage())) ?
				phrase.toLowerCase(locale) : phrase);
		if (text.isEmpty()) return null;

		if (isAny(text, "play", "phát", "phat", "resume", "tiếp tục", "tiep tuc", "oynat", "başlat", "devam et", "devam", "çal", "cal", "müziği çal", "muzigi cal"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.PLAY);
		if (isAny(text, "pause", "tạm dừng", "tam dung", "duraklat", "beklet", "oynatmayı duraklat"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.PAUSE);
		if (isAny(text, "stop", "dừng", "dung", "durdur", "oynatmayı durdur"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.STOP);
		if (isAny(text, "next", "next track", "next item", "bài tiếp theo", "bai tiep theo",
				"kênh tiếp theo", "kenh tiep theo", "tập tiếp theo", "tap tiep theo", "sonraki", "sonraki kanal", "bir sonraki kanal", "sonraki şarkı", "sonraki parça", "sonraki bölüm"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.NEXT);
		if (isAny(text, "previous", "previous track", "previous item", "bài trước",
				"bai truoc", "kênh trước", "kenh truoc", "tập trước", "tap truoc", "önceki", "önceki kanal", "bir önceki kanal", "önceki şarkı", "önceki parça", "önceki bölüm"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.PREVIOUS);
		if (isAny(text, "go back", "quay lại", "quay lai", "geri", "geri dön"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.BACK);
		if (isAny(text, "what is playing", "now playing", "đang phát gì", "dang phat gi", "ne çalıyor", "şu an oynatılan", "şu an ne çalıyor", "şu anda ne çalıyor"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.OPEN_CURRENT);
		if (isAny(text, "play favorites", "play my favorites", "phát mục yêu thích",
				"phat muc yeu thich", "favorileri oynat", "favorilerimi oynat", "oynat favoriler", "favorilerimi aç", "favorilerden oynat"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.PLAY_FAVORITES);

		int selection = parseSelectionIndex(text);
		if (selection >= 0) return VoiceIntent.selection(selection);

		// Turkish commonly places the verb after the content: "YouTube aç", "TV TRT 1 oynat".
		// Keep an already explicit action prefix intact, including English queries ending in a Turkish verb.
		if (locale != null && "tr".equals(locale.getLanguage()) && parseSearchAction(text) == null && !text.startsWith("altyazı")) {
			for (String verb : new String[]{"aç", "oynat", "ara", "bul", "izle", "seyret", "dinle"}) {
				if (text.endsWith(" " + verb)) {
					String action = ("izle".equals(verb) || "seyret".equals(verb) || "dinle".equals(verb)) ? "oynat" : verb;
					text = action + " " + text.substring(0, text.length() - verb.length()).trim();
					break;
				}
			}
		}
		VoiceIntent.SearchAction action = parseSearchAction(text);
		if (action == null) return null;
		String remainder = removeAction(text, action);
		if (remainder.isEmpty()) return null;

		String addon = null;
		for (Map.Entry<String, String> e : ADDON_ALIASES.entrySet()) {
			String alias = e.getKey();
			if (remainder.equals(alias) || remainder.startsWith(alias + " ")) {
				addon = e.getValue();
				remainder = remainder.substring(alias.length()).trim();
				break;
			}
		}

		if ((action == VoiceIntent.SearchAction.OPEN) && (addon != null) && remainder.isEmpty()) {
			return VoiceIntent.openAddon(addon);
		}
		remainder = removeContentQualifier(remainder);
		return remainder.isEmpty() ? null : VoiceIntent.search(action, addon, remainder);
	}

	/** Converts a MediaSession search query into the same deterministic in-app grammar. */
	@Nullable
	public static String mediaSearchCommand(@Nullable String query, @Nullable Locale locale) {
		if ((query == null) || query.isBlank()) return null;
		String command = query.trim();
		if (parse(command, locale) != null) return command;
		command = "play " + command;
		return (parse(command, locale) == null) ? null : command;
	}

	public static int parseSelectionIndex(@Nullable String phrase) {
		String text = normalize(phrase);
		if (text.isEmpty()) return -1;
		if (isAny(text, "1", "one", "first", "number one", "result one", "số một",
				"so mot", "kết quả một", "ket qua mot", "bir", "birinci", "birinci sonuç", "ilkini", "ilkini oynat", "birincisini", "ilk sonuç", "ilk sonucu oynat"))
			return 0;
		if (isAny(text, "2", "two", "second", "number two", "result two", "số hai",
				"so hai", "kết quả hai", "ket qua hai", "iki", "ikinci", "ikinci sonuç", "ikincisini", "ikinciyi", "ikinciyi oynat", "ikinci sonucu oynat"))
			return 1;
		if (isAny(text, "3", "three", "third", "number three", "result three", "số ba",
				"so ba", "kết quả ba", "ket qua ba", "üç", "üçüncü", "üçüncü sonuç", "üçüncüyü", "üçüncüsünü", "üçüncü sonucu oynat"))
			return 2;
		return -1;
	}

	private static VoiceIntent.SearchAction parseSearchAction(String text) {
		if (startsWithAny(text, "play ", "phát ", "phat ", "oynat "))
			return VoiceIntent.SearchAction.PLAY;
		if (startsWithAny(text, "find ", "search ", "tìm ", "tim ", "tìm kiếm ", "tim kiem ", "ara ", "bul "))
			return VoiceIntent.SearchAction.FIND;
		if (startsWithAny(text, "open ", "mở ", "mo ", "aç "))
			return VoiceIntent.SearchAction.OPEN;
		return null;
	}

	private static String removeAction(String text, VoiceIntent.SearchAction action) {
		String[] prefixes = switch (action) {
			case PLAY -> new String[]{"play ", "phát ", "phat ", "oynat "};
			case FIND -> new String[]{"find ", "search ", "tìm kiếm ", "tim kiem ", "tìm ", "tim ", "ara ", "bul "};
			case OPEN -> new String[]{"open ", "mở ", "mo ", "aç "};
		};
		for (String prefix : prefixes) if (text.startsWith(prefix)) return text.substring(prefix.length()).trim();
		return text;
	}

	private static String removeContentQualifier(String text) {
		String[] qualifiers = {"video ", "channel ", "kênh ", "kenh ", "bài ", "bai ",
				"movie ", "film ", "phim ", "series ",
				"episode ", "tập ", "tap ", "book ", "sách ", "sach ", "kanal ", "bölüm ", "kitap "};
		for (String qualifier : qualifiers) if (text.startsWith(qualifier)) return text.substring(qualifier.length()).trim();
		return text;
	}

	private static boolean startsWithAny(String text, String... prefixes) {
		for (String prefix : prefixes) if (text.startsWith(prefix)) return true;
		return false;
	}

	private static boolean isAny(String text, String... values) {
		for (String value : values) if (text.equals(value)) return true;
		return false;
	}

	private static String normalize(String phrase) {
		if (phrase == null) return "";
		String text = Normalizer.normalize(phrase, Normalizer.Form.NFKC).trim().toLowerCase(Locale.ROOT);
		text = text.replaceAll("[,.!?;:]+", " ").replaceAll("\\s+", " ");
		return text;
	}
}
