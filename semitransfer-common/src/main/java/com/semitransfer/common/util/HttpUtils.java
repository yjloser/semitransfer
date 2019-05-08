package com.semitransfer.common.util;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * http常用处理工具类
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-12-01 13:32
 * @version:2.0
 **/
public abstract class HttpUtils {

    /**
     * get有参请求
     *
     * @param url   请求地址
     * @param param 请求参数
     * @return 返回响应
     * @author Mr.Yang
     * @date 2018/12/1
     */
    public static String doGet(String url, Map<String, String> param) {
        // 创建Httpclient对象
        String resultString = "";
        CloseableHttpResponse response = null;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                param.forEach(builder::addParameter);
            }
            URI uri = builder.build();
            // 创建http GET请求
            HttpGet httpGet = new HttpGet(uri);
            // 执行请求
            response = httpclient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                resultString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    /**
     * get无参请求
     *
     * @param url 请求地址
     * @return 返回响应
     * @author Mr.Yang
     * @date 2018/12/1
     */
    public static String doGet(String url) {
        return doGet(url, null);
    }

    /**
     * post有参请求
     *
     * @param url   请求地址
     * @param param 请求参数
     * @author Mr.Yang
     * @date 2018/12/1
     */
    public static String doPost(String url, Map<String, String> param) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建参数列表
            if (param != null) {
                List<NameValuePair> paramList = new ArrayList<>();
                param.forEach((k, v) -> paramList.add(new BasicNameValuePair(k, v)));
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8);
                httpPost.setEntity(entity);
            }
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * post无参请求
     *
     * @param url 请求地址
     * @author Mr.Yang
     * @date 2018/12/1
     */
    public static String doPost(String url) {
        return doPost(url, null);
    }

    /**
     * post JSONG字符串请求
     *
     * @param url  请求地址
     * @param json json字符串
     * @author Mr.Yang
     * @date 2018/12/1
     */
    public static String doPostJson(String url, String json) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }
}
