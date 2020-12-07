package com.dhc.api.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


/**
 * Http Client工具类
 * @author dwj
 * @date
 */

public class RequestUtil {
    private static Logger logger = LoggerFactory.getLogger(RequestUtil.class);

    private static final int timeout = 120 * 1000;
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static CloseableHttpClient client;

    //设置请求超时时间， DEFAULT_CHARSET。可以后写到配置文件
    private final static RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();

    /**
     * 初始化httpClient, SSLContext信任所有
     * 
     * @return CloseableHttpClient
     */
    private static synchronized CloseableHttpClient getClient() {
        if (null == client) {
            SSLConnectionSocketFactory sslCSF = null;

            try {
                // 信任所有
                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
                sslCSF = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            client = HttpClients.custom().setSSLSocketFactory(sslCSF).build();
        }
        return client;
    }


    /**
     * get请求
     * @param url
     * @param params 参数
     * @return 请求结果
     */
    public static String doGet(String url, Map<String, String> params) {
        URI uri = generateURL(url, params);
        HttpGet get = new HttpGet(uri);
        get.setConfig(requestConfig);

        String res = execute(get);
        logger.debug("request: " + url + " response :" + res);
        return res;
    }

    /**
     * get请求
     * @param url
     * @param params url后参数
     * @param object request body对象 (可为 null)
     * @return 请求结果
     */
    public static String doPost(String url, Map<String, String> params, Object object, ContentType contentType) {
        if (null == contentType) {
            contentType = ContentType.APPLICATION_JSON;
        }

        URI uri = generateURL(url, params);
        HttpPost post = new HttpPost(uri);
        post.setConfig(requestConfig);

        String res = execute(post);
        logger.debug("request: " + url + " response :" + res);
        return res;
    }

   
    /**
     * 发起请求，关闭资源
     * @param request （HttpPost 或 HttpGet）
     * @return response 请求返回值
     */
    public static String execute(HttpUriRequest request) {
        String responseStr = null;
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = getClient().execute(request);
            responseStr = EntityUtils.toString(httpResponse.getEntity(), DEFAULT_CHARSET);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return responseStr;
    }

    /**
     * 生成请求的url
     * 
     * @param url 不带参数的url字符串
     * @param params 参数
     * 
     * @return URI， 请求的uri对象
     */
    private static URI generateURL(String url, Map<String, String> params) {
        URI uri = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            if (null != params) {
                for (Entry<String, String> entry : params.entrySet()) {
                    uriBuilder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
        }
        return uri;
    }
    
    public static String execute(String urlStr) {
        URI uri = RequestUtil.getUri(urlStr);
        HttpGet get = new HttpGet(uri);
        return execute(get, RequestUtil.DEFAULT_CHARSET);
    }

    public static String execute(HttpUriRequest request, String charset) {
        request.addHeader(HttpHeaders.CONNECTION, "close");
        String responseStr = null;
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = getClient().execute(request);
            responseStr = EntityUtils.toString(httpResponse.getEntity(), charset);
        } catch (IOException e) {
        	logger.error(e.getMessage());
        } finally {
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                	logger.error(e.getMessage());
                }
            }
        }
        return responseStr;
    }
    
    public static URI getUri(String urlStr) {
        URI uri = null;
        try {
            URL url = new URL(urlStr);
            uri = new URI(url.getProtocol(), null, url.getHost(), url.getPort(), url.getPath(), url.getQuery(), null);
        } catch (MalformedURLException e) {
        	logger.error(e.getMessage());
        } catch (URISyntaxException e) {
        	logger.error(e.getMessage());
        }
        
        return uri;
    }
}
