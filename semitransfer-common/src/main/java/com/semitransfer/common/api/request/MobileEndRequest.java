package com.semitransfer.common.api.request;

import com.alibaba.fastjson.JSONObject;
import com.semitransfer.common.api.AbstractRequest;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.api.response.MobileEndResponse;
import com.semitransfer.common.encrypt.AesUtils;
import com.semitransfer.common.encrypt.AnalyzeUtils;
import com.semitransfer.common.encrypt.RsaUtils;
import com.semitransfer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 移动端请求
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @create: 2018-12-02 16:01
 * @version:2.0
 **/
public class MobileEndRequest extends AbstractRequest<MobileEndResponse> {

    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(MobileEndRequest.class);

    /**
     * 解析请求数据
     *
     * @param request 请求类型
     * @return 返回解析得到的字符串
     * @author Mr.Yang
     * @date 2018/7/7
     */
    public static JSONObject requestMessage(HttpServletRequest request) {
        return requestMessage(request, null, null);
    }


    /**
     * 解析请求数据
     *
     * @param request      请求类型
     * @param fields       字符串数组-->校验请求参数
     * @param databaseType 数据库类型
     * @return 返回解析得到的字符串
     * @author Mr.Yang
     * @date 2018/7/7
     */
    public static JSONObject requestMessage(HttpServletRequest request, String databaseType, String... fields) {
        JSONObject params = new JSONObject();
        //获取requestParameter信息
        String requestParameter = request.getParameter(Constants.FIELD_PARA);
        //获取requestAttribute信息
        String requestAttribute = String.valueOf(request.getAttribute(Constants.FIELD_PARA));
        //从请求中获取数据
        if (StringUtils.notEmptyEnhance(requestParameter)
                || StringUtils.notEmptyEnhance(requestAttribute)) {
            // 将请求数据转为json字符串
            JSONObject requestParams = JSONObject.parseObject(StringUtils.notEmptyEnhance(requestParameter)
                    ? requestParameter : requestAttribute);
            // 解析密钥部分
            try {
                // 获取密钥信息
                String openKey = RsaUtils.decryptString(RsaUtils.getPrivateKey(), requestParams.getString(Constants.FIELD_KEY));
                // 如果不一致直接报错，让外层捕捉一下
                if (!openKey.equals(AnalyzeUtils.STATIC_KEYS)) {
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                logger.error("私钥解析AES密钥错误，检查密钥与配置的是否一致", e);
                // 解析失败
                params.put(Constants.FIELD_CODE, 40002);
                params.put(Constants.FIELD_MSG, "密钥不一致，解密失败");
                params.put(Constants.FIELD_CHECK_STATUS, false);
                return params;
            }
            // 解析参数部分
            try {
                // 解密
                byte[] decrypted = AesUtils.aesDecrypt(requestParams.getString(Constants.FIELD_PARAMS));
                // 获取参数结果
                params = JSONObject.parseObject(AesUtils.byteToStr(decrypted));
                if (fields != null) {
                    // 校验请求参数
                    for (String key : fields) {
                        // 判断是否存在该字段、字段是否为空
                        if (!params.containsKey(key) || StringUtils.isEmpty(params.getString(key))) {
                            // 请求参数缺失
                            params.put(Constants.FIELD_CHECK_STATUS, false);
                            params.put(Constants.FIELD_CODE, 40001);
                            params.put(Constants.FIELD_MSG, "请求缺少必选参数");
                            return params;
                        }
                    }
                }
                // 设置返回成功
                params.put(Constants.FIELD_CHECK_STATUS, true);
                // 判断是否存在分页问题
                if (!StringUtils.isEmpty(databaseType)) {
                    // 分页操作
                    checkPage(params, databaseType);
                }
                logger.info("{}|{}|{}|{}", 10000, getIpAddr(request), request.getRequestURI(), params.toString());
                return params;
            } catch (Exception e) {
                // 解析失败
                params.put(Constants.FIELD_CODE, 40002);
                params.put(Constants.FIELD_MSG, "密钥不一致，解密失败");
                params.put(Constants.FIELD_CHECK_STATUS, false);
                return params;
            }
        }
        return null;
    }
}
