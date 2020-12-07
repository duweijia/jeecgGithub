package com.dhc.api.utils;

import java.net.URL;
import java.util.Map;
import org.slf4j.Logger;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;
import java.security.cert.X509Certificate;



public class HttpClient {
	private final static Logger logger = LoggerFactory.getLogger(HttpClient.class);
	private String url;
	private String charset = "utf-8";
	private String httpMethod;
	private int connectTimeoutSeconds = 3;
	private int readTimeoutSeconds = 30;
	private Map<String, String> httpHeader;
	private HttpURLConnection con;
	OutputStreamWriter wr = null;
	BufferedReader in = null;
	private boolean isConnected = false;

	public HttpClient(String url, String httpMethod, String charset, int connectTimeoutSeconds, int readTimeoutSeconds,
			Map<String, String> httpHeader) {
		this.url = url;
		this.httpMethod = httpMethod.toUpperCase();
		if (charset != null) {
			this.charset = charset;
		}
		if (connectTimeoutSeconds > 0) {
			this.connectTimeoutSeconds = connectTimeoutSeconds;
		}
		if (readTimeoutSeconds > 0) {
			this.readTimeoutSeconds = readTimeoutSeconds;
		}
		this.httpHeader = httpHeader;
	}

	public void connect() throws IOException {
		con = createConnection();
		con.connect();
		isConnected = true;
	}

    /**
     *
     * @return
     * @throws IOException
     */
	public String send(String value) throws IOException {
		try {
			if (!isConnected) {
				connect();
			}
			//logger.info("send http msg begin: {}", value);
			if (value != null && !value.isEmpty()) {
				wr = new OutputStreamWriter(con.getOutputStream(), charset);
				wr.write(value);
				wr.flush();
			}
			int responseCode = con.getResponseCode();
			//logger.info("HTTP Response Code:{}", responseCode);
			if (responseCode == 200) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
			} else {
				in = new BufferedReader(new InputStreamReader(con.getErrorStream(), charset));
			}
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			String ret = response.toString();
			return ret;
		} catch (IOException e) {
			logger.error("", e);
			throw e;
		} finally {
			close();
		}
	}

	public void close() {
		if (wr != null) {
			try {
				wr.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		if (con != null) {
			con.disconnect();
		}
	}

	private HttpURLConnection createConnection() throws IOException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod(httpMethod);
		con.setRequestProperty("Accept-Charset", charset);
		con.setUseCaches(false);// 取消缓存
		con.setRequestProperty("Content-type", "application/json;charset=" + charset);
		if (httpHeader != null && !httpHeader.isEmpty()) {
			for (Map.Entry<String, String> header : httpHeader.entrySet()) {
				con.setRequestProperty(header.getKey(), header.getValue());
			}
		}
		con.setReadTimeout(readTimeoutSeconds * 1000);
		con.setConnectTimeout(connectTimeoutSeconds * 1000);
		con.setDoOutput(true);
		con.setDoInput(true);
		if ("https".equalsIgnoreCase(obj.getProtocol())) {
			trustAllHosts();
			HttpsURLConnection cons = (HttpsURLConnection) con;
			cons.setHostnameVerifier(DO_NOT_VERIFY);
			return cons;
		}
		return con;
	}

	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	private static void trustAllHosts() {

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain, String authType) {

			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) {

			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
