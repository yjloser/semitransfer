package com.semitransfer.common.api.request;

import com.alibaba.fastjson.JSONObject;
import com.semitransfer.common.api.AbstractRequest;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.api.response.DesktopEndResponse;
import com.semitransfer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 桌面端请求
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @create: 2018-12-02 16:08
 * @version:2.0
 **/
public class DesktopEndRequest extends AbstractRequest<DesktopEndResponse> {

    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(DesktopEndRequest.class);

    /**
     * 从request中获取请求信息
     *
     * @param request 解析信息
     * @return 返回处理的信息
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject requestMessage(HttpServletRequest request) {
        return requestMessage(request, null);
    }


    /**
     * 从request中获取请求信息
     *
     * @param request 解析信息
     * @param fields  必填字段
     * @return 返回处理的信息
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject requestMessage(HttpServletRequest request, String... fields) {
        //获取requestParameter信息
        String requestParameter = request.getParameter(Constants.FIELD_PARAMS);
        //获取requestAttribute信息
        String requestAttribute = String.valueOf(request.getAttribute(Constants.FIELD_PARAMS));
        //从请求中获取数据
        if (StringUtils.notEmptyEnhance(requestParameter)
                || StringUtils.notEmptyEnhance(requestAttribute)) {
            //获取参数
            String requestParams = StringUtils.notEmptyEnhance(requestParameter) ? requestParameter : requestAttribute;
            try {
                //转换json格式
                JSONObject params = JSONObject.parseObject(requestParams);
                //获取头部loginkey
                params.put(Constants.FIELD_LOGIN_KEY,
                        StringUtils.isEmptyEnhance(request.getHeader(Constants.FIELD_TOKEN)) ? null : request.getHeader(Constants.FIELD_TOKEN));
                if (fields != null) {
                    // 校验请求参数
                    for (String key : fields) {
                        // 判断是否存在该字段、字段是否为空
                        if (!params.containsKey(key) || StringUtils.isEmpty(params.getString(key))) {
                            // 请求参数缺失
                            params.put(Constants.FIELD_CODE, 40001);
                            params.put(Constants.FIELD_MSG, "请求缺少必选参数");
                            params.put(Constants.FIELD_CHECK_STATUS, false);
                            return params;
                        }
                    }
                }
                //处理分页页面
                checkPage(params);
                return params;
            } catch (Exception e) {
                logger.error("请求信息转换为JSON格式报错", e);
                e.printStackTrace();
                return null;
            }
        }
        //转换json格式
        JSONObject params = new JSONObject();
        //获取头部loginkey
        params.put(Constants.FIELD_LOGIN_KEY,
                StringUtils.isEmptyEnhance(request.getHeader(Constants.FIELD_TOKEN)) ? null : request.getHeader(Constants.FIELD_TOKEN));
        return params;
    }
}
