package com.cvt.iri.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * HTTP工具类
 *
 * @author <a href="mailto:huangfengjing@gmail.com">Ivan</a>
 * @version 1.01
 */
public final class HttpHelper {

    private static Log logger = LogFactory.getLog("HttpHelper");

    public static final String CHARACTER = "utf-8";

    public static int CONNECTION_TIMEOUT = 1 * 60 * 1000;
    public static int SOCKET_TIMEOUT = 1 * 60 * 1000;

    /**
     * 工具类不要实例化，BODY 注释纯属搞笑，请勿当真
     */
    private HttpHelper() {
        // u son of bitch, do not initialize me, i am just a tool, r u an idiot
        // ?
    }

    public static HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(CONNECTION_TIMEOUT);
        client.getHttpConnectionManager().getParams().setSoTimeout(SOCKET_TIMEOUT);

        return client;
    }

    public static String urlDecode(String text) {
        try {
            URLDecoder.decode(text, CHARACTER);
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        return text;
    }

    public static String urlEncode(String text) {
        try {
            URLEncoder.encode(text, CHARACTER);
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        return text;
    }

    /**
     * 从指定的URL地址取得HTTP应答文本 ( GET )
     *
     * @param urlStr url地址
     * @return 字符串表示的下载数据
     */
    public static String get(String urlStr) {
        return get(urlStr, null);
    }

    public static JSONObject getJson(String urlStr) {
        return getJson(urlStr, null);
    }

    /**
     * 从指定的URL地址取得HTTP应答文本 ( GET )
     *
     * @param urlStr url地址
     * @return 字符串表示的下载数据
     */
    public static String get(String urlStr, Map<String, Object> params) {
        try {
            String result = IOUtils.toString(getInputStream(urlStr, params));
            logger.debug(urlStr + " 请求结果：" + result);
            return result;
        } catch (Exception e) {
            logger.error("请求错误,url:" + urlStr + params, e);
            return "";
        }
    }
    public static JSONObject getJson(String urlStr, Map<String, Object> params) {
        try {
            return JSON.parseObject(get(urlStr, params));
        } catch (Exception e) {
            logger.error("获取 JSON 数据失败", e);
            return null;
        }
    }

    public static InputStream getInputStream(String urlStr, Map<String, Object> params) {
        if (null == params) {
            params = new HashMap<String, Object>();
        }
        logger.info("GET 请求 URL：" + urlStr + params);
        if (urlStr.contains("?")) {
            urlStr += "&" + UrlUtils.map2Query(params);
        } else {
            urlStr += "?" + UrlUtils.map2Query(params);
        }
        GetMethod getMethod = new GetMethod(urlStr);
        try {
            int code = getHttpClient().executeMethod(getMethod);
            if(code == HttpStatus.SC_OK) {
                return getMethod.getResponseBodyAsStream();
            }
            logger.error("请求失败,url ：" + urlStr + ", code:" + code);
            return null;
        } catch (IOException e) {
            logger.error("请求错误,url:" + urlStr + params, e);
            return null;
        }
    }

    /**
     * 从指定的URL地址取得HTTP应答文本 ( GET )
     *
     * @param urlStr url地址
     * @return 字符串表示的下载数据
     */
    public static String post(String urlStr, Map<String, Object> params) {
        if (null == params) {
            params = new HashMap<String, Object>();
        }
        logger.info("POST 请求 URL：" + urlStr + params);
        PostMethod postMethod = new PostMethod(urlStr);
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        for (String key : params.keySet()) {
            postParameters.add(new NameValuePair(key, "" + params.get(key)));
        }
        postMethod.setRequestBody(postParameters.toArray(new NameValuePair[postParameters.size()]));
        try {
            int code = getHttpClient().executeMethod(postMethod);
            if(code != HttpStatus.SC_OK) {
                logger.error("请求失败,url ：" + urlStr + ", code:" + code);
                return "";
            }
            return IOUtils.toString(postMethod.getResponseBodyAsStream());
        } catch (IOException e) {
            logger.error("请求错误,url:" + urlStr + params, e);
            return "";
        }
    }

    public static JSONObject post4Json(String urlStr) {
        return post4Json(urlStr, null);
    }

    public static JSONObject post4Json(String urlStr, Map<String, Object> params) {
        String result = post(urlStr, params);
        if (StringUtils.isBlank(result)) {
            return null;
        }
        try {
            return JSON.parseObject(result);
        } catch (Exception e) {
            logger.error("解析返回数据异常！", e);
            return null;
        }
    }

    /**
     * 从服务器获取 JSON 对象
     *
     * @param url 服务器 URL
     * @return JSON 对象
     */
    public static <T> T getJSONObject(String url, Class<T> cls) {
        return getJSONObject(url, null, cls);
    }

    /**
     * 从服务器获取 JSON 对象
     *
     * @param url 服务器 URL
     * @return JSON 对象
     */
    public static <T> T getJSONObject(String url, Map<String, Object> params, Class<T> cls) {
        String json = get(url, params);
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return JSONObject.parseObject(json, cls);
        } catch (Exception e) {
            logger.error("解析返回的字符串失败:" + json, e);
            return null;
        }
    }

    /**
     * 从服务器获取 JSON 对象
     *
     * @param url 服务器 URL
     * @return JSON 对象
     */
    public static <T> List<T> getJSONObjectArray(String url, Class<T> cls) {
        return getJSONObjectArray(url, null, cls);
    }

    /**
     * 从服务器获取 JSON 对象
     *
     * @param url 服务器 URL
     * @return JSON 对象
     */
    public static <T> List<T> getJSONObjectArray(String url, Map<String, Object> params, Class<T> cls) {
        String json = get(url, params);
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return JSONObject.parseArray(json, cls);
        } catch (Exception e) {
            logger.error("解析返回的字符串失败:" + json, e);
            return Collections.emptyList();
        }
    }
}
