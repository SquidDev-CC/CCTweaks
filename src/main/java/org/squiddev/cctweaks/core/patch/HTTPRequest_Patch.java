package org.squiddev.cctweaks.core.patch;

import dan200.computercraft.ComputerCraft;
import org.squiddev.cctweaks.core.lua.HTTPResponse;
import org.squiddev.patcher.visitors.MergeVisitor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

@MergeVisitor.Rename(
	from = "org/squiddev/cctweaks/core/patch/HTTPRequest_Patch$HTTPRequestException",
	to = "dan200/computercraft/core/apis/HTTPRequestException"
)
@MergeVisitor.Rewrite
public class HTTPRequest_Patch {
	@MergeVisitor.Stub
	public static class HTTPRequestException extends Exception {
		private static final long serialVersionUID = 1626499674233098258L;

		public HTTPRequestException(String s) {
			super(s);
		}
	}

	public static URL checkURL(String urlString) throws HTTPRequestException {
		URL url;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			throw new HTTPRequestException("URL malformed");
		}

		String protocol = url.getProtocol().toLowerCase();
		if (!protocol.equals("http") && !protocol.equals("https")) throw new HTTPRequestException("URL not http");

		boolean allowed = false;
		String whitelistString = ComputerCraft.http_whitelist;
		String[] allowedURLs = whitelistString.split(";");
		for (String allowedURL : allowedURLs) {
			Pattern allowedURLPattern = Pattern.compile("^\\Q" + allowedURL.replaceAll("\\*", "\\\\E.*\\\\Q") + "\\E$");
			if (allowedURLPattern.matcher(url.getHost()).matches()) {
				allowed = true;
				break;
			}
		}

		if (!allowed) throw new HTTPRequestException("Domain not permitted");

		return url;
	}

	private final Object lock = new Object();
	private URL url;
	private final String urlString;
	private boolean complete = false;
	private boolean cancelled = false;
	private boolean success = false;
	private byte[] result;
	private int responseCode = -1;

	public HTTPRequest_Patch(String url, final String postText, final Map<String, String> headers) throws HTTPRequestException {
		urlString = url;
		this.url = checkURL(url);

		Thread thread = new Thread(new Runnable() {
			@MergeVisitor.Rewrite
			protected boolean ANNOTATION;

			@Override
			public void run() {
				try {
					HttpURLConnection connection = (HttpURLConnection) HTTPRequest_Patch.this.url.openConnection();

					{ // Setup connection
						if (postText != null) {
							connection.setRequestMethod("POST");
							connection.setDoOutput(true);
						} else {
							connection.setRequestMethod("GET");
						}

						if (headers != null) {
							for (Map.Entry header : headers.entrySet()) {
								connection.setRequestProperty((String) header.getKey(), (String) header.getValue());
							}
						}

						if (postText != null) {
							OutputStream os = connection.getOutputStream();
							OutputStreamWriter osr = new OutputStreamWriter(os);
							BufferedWriter writer = new BufferedWriter(osr);
							writer.write(postText, 0, postText.length());
							writer.close();
						}
					}

					// Read from the input stream
					InputStream is = connection.getInputStream();
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					int nRead;
					byte[] data = new byte[1024];
					while ((nRead = is.read(data, 0, data.length)) != -1) {
						synchronized (lock) {
							if (cancelled) break;
						}

						buffer.write(data, 0, nRead);
					}
					is.close();

					synchronized (lock) {
						if (cancelled) {
							complete = true;
							success = false;
							result = null;
						} else {
							complete = true;
							success = true;
							result = buffer.toByteArray();
							responseCode = connection.getResponseCode();
						}
					}

					connection.disconnect();
				} catch (IOException e) {
					synchronized (lock) {
						complete = true;
						success = false;
						result = null;
					}
				}
			}
		});
		thread.start();
	}

	public String getURL() {
		return urlString;
	}

	public void cancel() {
		synchronized (lock) {
			cancelled = true;
		}
	}

	public boolean isComplete() {
		synchronized (lock) {
			return complete;
		}
	}

	public boolean wasSuccessful() {
		synchronized (lock) {
			return success;
		}
	}

	public HTTPResponse asResponse() {
		synchronized (lock) {
			return new HTTPResponse(responseCode, result);
		}
	}

}
