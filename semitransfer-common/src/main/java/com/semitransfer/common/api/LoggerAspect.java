package com.semitransfer.common.api;

import com.alibaba.fastjson.JSONObject;
import com.semitransfer.common.util.DateUtils;
import com.semitransfer.common.util.StringUtils;
import com.semitransfer.plus.config.internal.ConfigConstants;
import com.semitransfer.plus.config.internal.annotation.LoggerManage;
import com.semitransfer.plus.config.internal.util.redis.RedisTempalte;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.semitransfer.common.api.AbstractRequest.getIpAddr;
import static com.semitransfer.common.api.BaseRquestAndResponse.getRequest;

/**
 * <p>
 * 日志拦截
 * </p>
 *
 * @author Mr.Yang
 * @since 2019-02-13
 */
@Aspect
@Component
@Order(1)
public class LoggerAspect {

    private static Logger logger = LoggerFactory.getLogger(LoggerAspect.class);

    /**
     * Redis缓存
     */
    @Autowired
    private RedisTempalte redisTemplate;

    /**
     * 日志记录前置处理
     *
     * @param loggerManage 日志注解
     * @author Mr.Yang
     * @date 2019/2/13 0013
     */
    @Before("@annotation(loggerManage)")
    public void beforeLogger(LoggerManage loggerManage) {
        //获取请求和响应
        HttpServletRequest request = getRequest();
        //是否存在请求值
        //权限码
        String aclCode = null, reqParams = null, operatorName = null;
        if (StringUtils.notEmptyEnhance(request.getParameter(Constants.FIELD_PARAMS))) {
            //获取请求数据
            reqParams = request.getParameter(Constants.FIELD_PARAMS);
            //转换
            JSONObject strToJson = JSONObject.parseObject(reqParams);
            //是否存在权限码
            if (strToJson.containsKey(Constants.POWER_CODE)) {
                aclCode = strToJson.getString(Constants.POWER_CODE);
            }
        }
        //操作姓名
        if (StringUtils.notEmptyEnhance(request.getAttribute(Constants.FIELD_OPERATOR_NAME))) {
            operatorName = request.getAttribute(Constants.FIELD_OPERATOR_NAME).toString();
        }
        //获取请求ip地址
        String reqIp = getIpAddr(request);
        //获取请求链接
        String requestLinke = request.getRequestURI();
        //组合日志对象
        Map<String, String> loggerMap = new HashMap<>(12);
        //接口 参数 模块 操作者 操作时间 操作ip
        loggerMap.put(Constants.FIELD_API, requestLinke.concat("-").concat(StringUtils.notEmpty(aclCode) ? aclCode : ""));
        loggerMap.put(Constants.FIELD_PARAMS, reqParams);
        loggerMap.put(Constants.FIELD_MODULE, loggerManage.module());
        loggerMap.put(Constants.FIELD_OPERATOR, operatorName);
        loggerMap.put(Constants.FIELD_OPERATOR_TIME, DateUtils.getCurDateTimeStr());
        loggerMap.put(Constants.FIELD_OPERATOR_IP, reqIp);
        //map转json
        JSONObject mapToJson = new JSONObject();
        mapToJson.putAll(loggerMap);
        try {
            //压入缓存
            this.redisTemplate.listPush(ConfigConstants.NUM_ZERO, Constants.FIELD_LOGGER_LIST, mapToJson.toJSONString());
        } catch (Exception e) {
            logger.error("日志拦截写入失败");
        }
        //输出日志
        switch (loggerManage.level()) {
            case WARN:
                logger.warn("日志拦截信息:{}", mapToJson);
                break;
            case INFO:
                logger.info("日志拦截信息:{}", mapToJson);
                break;
            case ERROR:
                logger.error("日志拦截信息:{}", mapToJson);
                break;
            default:
                logger.debug("日志拦截信息:{}", mapToJson);
                break;
        }
    }
}
