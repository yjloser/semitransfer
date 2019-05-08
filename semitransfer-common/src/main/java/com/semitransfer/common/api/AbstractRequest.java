package com.semitransfer.common.api;

import com.alibaba.fastjson.JSONObject;
import com.semitransfer.plus.config.internal.ConfigEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 基础请求类
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @create: 2018-11-03 12:59
 * @version:2.0
 **/
public abstract class AbstractRequest<T extends AbstractResponse> {

    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(AbstractRequest.class);

    /**
     * 获取登录用户IP地址
     *
     * @param request 请求包装类
     * @return 返回用户真正的端口
     * @author Mr.Yang
     * @date 2018/7/4 0004
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (null == ip || ip.length() == Constants.NUM_ZERO || Constants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (null == ip || ip.length() == Constants.NUM_ZERO || Constants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");

        }
        if (null == ip || ip.length() == Constants.NUM_ZERO || Constants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (Constants.LOCALHOST.equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    /**
     * 数据库分页操作
     *
     * @param outcome 请求参数
     * @author Mr.Yang
     * @date 2018/7/7
     */
    public static void checkPage(JSONObject outcome) {
        checkPage(outcome, outcome.containsKey(Constants.FIELD_DATABASE_TYPE) ?
                outcome.getString(Constants.FIELD_DATABASE_TYPE) : ConfigEnum.MYSQL_JDBC.getKey());
    }

    /**
     * 数据库分页操作
     *
     * @param outcome      请求参数
     * @param databaseType 数据库类型
     * @author Mr.Yang
     * @date 2018/7/7
     */
    public static void checkPage(JSONObject outcome, String databaseType) {
        // 起始页
        int currentPage = 1;
        // 每条数
        int pageSize;
        try {
            currentPage = outcome.getIntValue(Constants.FIELD_CURRENT);
            pageSize = outcome.containsKey(Constants.FIELD_SIZE) ?
                    outcome.getIntValue(Constants.FIELD_SIZE) : Constants.NUM_TEN;
        } catch (Exception e) {
            logger.error("解析数据库分页时,请求参数未解析到分页字段.", e);
            // 解析失败
            outcome.put(Constants.FIELD_CHECK_STATUS, false);
            return;
        }
        // 实体类分页
        outcome.put(Constants.FIELD_BIGENPAGE, (((currentPage - 1) * pageSize)));
        outcome.put(Constants.FIELD_SIZE, pageSize);
        // 数据库类型
        if (Constants.FIELD_ORACLE.equals(databaseType)) {
            outcome.put(Constants.FIELD_ENDPAGE, pageSize * currentPage);
        }
    }


}
