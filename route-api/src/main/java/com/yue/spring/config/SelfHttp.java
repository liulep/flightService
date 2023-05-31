package com.yue.spring.config;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.client.utils.ParamUtil;
import com.alibaba.nacos.client.*;
import com.alibaba.nacos.common.constant.HttpHeaderConsts;
import com.alibaba.nacos.common.utils.IoUtils;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.alibaba.nacos.common.utils.UuidUtils;
import com.alibaba.nacos.common.utils.VersionUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class SelfHttp {

    public static HttpResult doGet(String url, String encoding, List<String> paramValues) throws IOException {
        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(Math.max(ParamUtil.getConnectTimeout(), 100));
            conn.setReadTimeout(5000);
            List<String> newHeaders = getHeaders(url, new ArrayList<>(), paramValues);
            setHeaders(conn, newHeaders, encoding);

            conn.connect();

            int respCode = conn.getResponseCode();
            String resp = null;

            if (HttpURLConnection.HTTP_OK == respCode) {
                resp = IoUtils.toString(conn.getInputStream(), encoding);
            } else {
                resp = IoUtils.toString(conn.getErrorStream(), encoding);
            }
            return new HttpResult(respCode, conn.getHeaderFields(), resp);
        } finally {
            IoUtils.closeQuietly(conn);
        }
    }

    public static List<String> getHeaders(String url, List<String> headers, List<String> paramValues)
            throws IOException {
        List<String> newHeaders = new ArrayList<String>();
        newHeaders.add("exConfigInfo");
        newHeaders.add("true");
        newHeaders.add("RequestId");
        newHeaders.add(UuidUtils.generateUuid());
        if (headers != null) {
            newHeaders.addAll(headers);
        }
        return newHeaders;
    }

    static public void setHeaders(HttpURLConnection conn, List<String> headers, String encoding) {
        if (null != headers) {
            for (Iterator<String> iter = headers.iterator(); iter.hasNext(); ) {
                conn.addRequestProperty(iter.next(), iter.next());
            }
        }
        conn.addRequestProperty(HttpHeaderConsts.CLIENT_VERSION_HEADER, VersionUtils.version);
        conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + encoding);

        String ts = String.valueOf(System.currentTimeMillis());
        String token = MD5Utils.encodeHexString((ts + ParamUtil.getAppKey()).getBytes(StandardCharsets.UTF_8));

        conn.addRequestProperty(Constants.CLIENT_APPNAME_HEADER, ParamUtil.getAppName());
        conn.addRequestProperty(Constants.CLIENT_REQUEST_TS_HEADER, ts);
        conn.addRequestProperty(Constants.CLIENT_REQUEST_TOKEN_HEADER, token);
    }

    static public class HttpResult {
        final public int code;
        final public Map<String, List<String>> headers;
        final public String content;

        public HttpResult(int code, String content) {
            this.code = code;
            this.headers = null;
            this.content = content;
        }

        public HttpResult(int code, Map<String, List<String>> headers, String content) {
            this.code = code;
            this.headers = headers;
            this.content = content;
        }
    }
}
