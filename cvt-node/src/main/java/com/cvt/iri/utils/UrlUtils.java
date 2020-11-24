/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cvt.iri.utils;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * URL 相关的工具
 */
abstract public class UrlUtils {

    public static String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // ignore
            return text;
        }
    }

    /**
     * 构造 URL
     *
     * @param r 请求信息
     * @return 完整的 URL 地址
     */
    public static String buildFullRequestUrl(HttpServletRequest r) {
        return buildFullRequestUrl(r.getScheme(), r.getServerName(), r.getServerPort(), r.getRequestURI(),
                r.getQueryString());
    }

    /**
     * 构造 URL
     *
     * @param r 请求信息
     * @return 完整的 URL 地址
     */
    public static String buildFullRequestUrl(HttpServletRequest r, String requestUri, String query) {
        return buildFullRequestUrl(r.getScheme(), r.getServerName(), r.getServerPort(), requestUri, query);
    }

    /**
     * 构造 URL
     *
     * @param scheme      请求 schema
     * @param serverName  服务器名称
     * @param serverPort  服务器端口
     * @param requestURI  请求前缀
     * @param queryString 请求参数
     * @return 完整的 URL 地址
     */
    public static String buildFullRequestUrl(String scheme, String serverName, int serverPort, String requestURI,
                                             String queryString) {

        scheme = scheme.toLowerCase();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if ("http".equals(scheme)) {
            if (serverPort != 80) {
                url.append(":").append(serverPort);
            }
        } else if ("https".equals(scheme)) {
            if (serverPort != 443) {
                url.append(":").append(serverPort);
            }
        }

        if (!StringUtils.startsWith(requestURI, "/")) {
            url.append("/");
        }
        url.append(requestURI);

        if (StringUtils.isNotBlank(queryString)) {
            url.append("?").append(queryString);
        }

        return url.toString();
    }

    /**
     * 根据原请求，和新的请求路径参数等构造新的请求 URL
     *
     * @param request     原请求
     * @param requestURI  新的路径
     * @param queryString 新的参数
     * @return 新的 URL
     */
    public static String changeFullRequestUrl(HttpServletRequest request, String requestURI, String queryString) {
        return buildFullRequestUrl(request.getScheme(), request.getServerName(), request.getServerPort(), requestURI, queryString);
    }

    /**
     * 判断是否合法的重定向地址，以 / 或者 http 开头
     *
     * @param url 待判断的地址
     * @return 是否合法的标识
     */
    public static boolean isValidRedirectUrl(String url) {
        return url != null && (url.startsWith("/") || url.toLowerCase().startsWith("http"));
    }

    /**
     * 解析 URL 中的参数值
     *
     * @param url       URL字符串
     * @param paramName 要解析的参数值
     * @return 参数值
     */
    public static String parseParamValue(String url, String paramName) {
        if (StringUtils.isBlank(url) || StringUtils.isBlank(paramName) || !url.contains(paramName) || !url.contains("?")) {
            return "";
        }
        String query = url.substring(url.lastIndexOf("?") + 1);
        for (String pair : query.split("&")) {
            String[] tmp = pair.split("=");
            if (tmp.length == 2 && paramName.equals(tmp[0])) {
                return tmp[1];
            }
        }
        return "";
    }

    /**
     * 将 MAP 串接为 URL QUERY
     *
     * @param params 参数
     * @return url query
     */
    public static String map2Query(Map<String, Object> params) {
        if (null == params || params.isEmpty()) {
            return "";
        }
        boolean isFirst = true;
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            if (!isFirst) {
                sb.append("&");
            }
            sb.append(key).append("=").append(urlEncode(params.get(key).toString()));
            isFirst = false;
        }
        return sb.toString();
    }

    /**
     * 将参数串接到 url 后面
     *
     * @param url    url
     * @param params 参数
     * @return 新的 url
     */
    public static String appendParams(String url, Map<String, Object> params) {
        String query = map2Query(params);
        if (url.contains("?")) {
            return url += "&" + query;
        }
        return url += "?" + query;
    }

    /**
     * 将参数格式的字符串转换为 map
     *
     * @param query 参数格式的字符串
     * @return map
     */
    public static Map<String, String> query2Map(String query) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<String, String>();
        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=");
            if (keyValue.length != 2) {
                continue;
            }
            map.put(keyValue[0], keyValue[1]);
        }

        return map;
    }
}
