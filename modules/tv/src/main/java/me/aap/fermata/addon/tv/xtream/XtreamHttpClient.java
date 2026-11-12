package me.aap.fermata.addon.tv.xtream;

import static me.aap.utils.async.Completed.failed;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import me.aap.utils.app.App;
import me.aap.utils.async.FutureSupplier;

final class XtreamHttpClient {
	private final XtreamAccount account;
	private final XtreamErrorMapper errors;

	XtreamHttpClient(XtreamAccount account, XtreamErrorMapper errors) {
		this.account = account;
		this.errors = errors;
	}

	<T> FutureSupplier<T> get(@Nullable String action,
													 @Nullable Map<String, String> extraParams,
													 ResponseParser<T> parser) {
		String url = account.buildPlayerApiUrl(action, extraParams);
		URL requestUrl;

		try {
			requestUrl = new URL(url);
		} catch (IOException ex) {
			return failed(errors.map(ex));
		}

		return App.get().getExecutor().submitTask(() -> {
			HttpURLConnection connection = null;

			try {
				connection = openConnection(requestUrl, "gzip, deflate");
				int status = connection.getResponseCode();
				if (status != HttpURLConnection.HTTP_OK) {
					close(connection.getErrorStream());
					throw errors.httpStatus(status, connection.getResponseMessage());
				}

				InputStream payload = connection.getInputStream();
				if (payload == null) throw new IOException("Xtream response is empty");
				try (InputStream in = decode(payload, connection.getContentEncoding())) {
					return parser.parse(in);
				}
			} catch (Throwable ex) {
				throw errors.map(ex);
			} finally {
				if (connection != null) connection.disconnect();
			}
		});
	}

	<T> FutureSupplier<T> probe(String url, ProbeHandler<T> handler) {
		URL requestUrl;

		try {
			requestUrl = new URL(url);
		} catch (IOException ex) {
			return failed(errors.map(ex));
		}

		return App.get().getExecutor().submitTask(() -> {
			HttpURLConnection connection = null;

			try {
				connection = openConnection(requestUrl, "identity");
				connection.setRequestProperty("Range", "bytes=0-0");
				int status = connection.getResponseCode();
				close((status >= HttpURLConnection.HTTP_BAD_REQUEST) ?
						connection.getErrorStream() : connection.getInputStream());
				return handler.handle(status, connection.getResponseMessage());
			} catch (Throwable ex) {
				throw errors.map(ex);
			} finally {
				if (connection != null) connection.disconnect();
			}
		});
	}

	private HttpURLConnection openConnection(URL requestUrl, String acceptEncoding)
			throws IOException {
		URL current = requestUrl;
		for (int redirects = 0; ; redirects++) {
			String scheme = current.getProtocol();
			if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
				throw new IOException("Xtream request destination is invalid");
			}
			HttpURLConnection connection = (HttpURLConnection) current.openConnection();
			try {
				connection.setRequestMethod("GET");
				connection.setInstanceFollowRedirects(false);
				connection.setRequestProperty("Accept-Encoding", acceptEncoding);
				String userAgent = account.getUserAgent();
				if (userAgent != null) connection.setRequestProperty("User-Agent", userAgent);
				int timeout = account.getResponseTimeout();
				if (timeout > 0) {
					int millis = (int) Math.min(Integer.MAX_VALUE, timeout * 1000L);
					connection.setConnectTimeout(millis);
					connection.setReadTimeout(millis);
				}
				int status = connection.getResponseCode();
				if (!isRedirect(status)) return connection;
				if (redirects >= 5) throw new IOException("Xtream request exceeded the redirect limit");
				String location = connection.getHeaderField("Location");
				if ((location == null) || location.isBlank()) {
					throw new IOException("Xtream redirect did not include a destination");
				}
				URL next = new URL(current, location);
				if (isHttpsDowngrade(current, next) || !sameOrigin(current, next)) {
					throw new IOException("Xtream redirect to an untrusted destination was blocked");
				}
				close(connection.getErrorStream());
				close(connection.getInputStream());
				connection.disconnect();
				current = next;
			} catch (IOException | RuntimeException ex) {
				connection.disconnect();
				throw ex;
			}
		}
	}

	static boolean isHttpsDowngrade(URL current, URL next) {
		return "https".equalsIgnoreCase(current.getProtocol()) &&
				"http".equalsIgnoreCase(next.getProtocol());
	}

	private static boolean isRedirect(int status) {
		return status == HttpURLConnection.HTTP_MOVED_PERM ||
				status == HttpURLConnection.HTTP_MOVED_TEMP ||
				status == HttpURLConnection.HTTP_SEE_OTHER || status == 307 || status == 308;
	}

	private static boolean sameOrigin(URL first, URL second) {
		return first.getProtocol().equalsIgnoreCase(second.getProtocol()) &&
				first.getHost().equalsIgnoreCase(second.getHost()) &&
				first.getPort() == second.getPort();
	}

	private InputStream decode(InputStream in, CharSequence encoding) throws IOException {
		if (encoding == null) return in;
		String value = encoding.toString().toLowerCase(Locale.ROOT);
		if (value.contains("gzip")) return new GZIPInputStream(in);
		if (value.contains("deflate")) return new InflaterInputStream(in);
		throw new IOException("Unsupported Xtream content encoding: " + encoding);
	}

	private void close(@Nullable InputStream in) throws IOException {
		if (in != null) in.close();
	}

	interface ResponseParser<T> {
		T parse(InputStream in) throws IOException;
	}

	interface ProbeHandler<T> {
		T handle(int status, String reason) throws IOException;
	}
}
