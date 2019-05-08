package com.semitransfer.common.api.response;

import com.semitransfer.common.api.AbstractResponse;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.encrypt.AesUtils;
import com.semitransfer.common.encrypt.RsaCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

import static com.semitransfer.common.encrypt.AnalyzeUtils.getCodeValue;

/**
 * 移动端响应
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @create: 2018-12-02 16:01
 * @version:2.0
 **/
public class MobileEndResponse extends AbstractResponse {

    private static final long serialVersionUID = 6635153930520851477L;

    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(MobileEndResponse.class);

    /**
     * 默认成功
     *
     * @param response 响应对象
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(HttpServletResponse response) {
        responseMessage(null, response);
    }

    /**
     * 无加密输出 业务成功
     *
     * @param body     业务提示
     * @param response 响应对象
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(String body, HttpServletResponse response) {
        responseMessage(body, false, response);
    }

    /**
     * 加密输出 业务成功
     *
     * @param body      业务提示
     * @param isEncrypt 是否加密
     * @param response  响应对象
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(String body, boolean isEncrypt, HttpServletResponse response) {
        //是否加密输出
        if (isEncrypt) {
            // 使用AES加密
            byte[] encrypted = AesUtils.aesEncrypt(body);
            // 转换返回类型
            try {
                body = RsaCoder.encryptBASE64(encrypted);
            } catch (Exception e) {
                logger.error("对返回数据进行加密错误,请检查参数是否配置正确.", e);
                //失败直接写出
                write(new MobileEndResponse("40003", "加密异常", null), response);
                return;
            }
        }
        //直接写出
        write(new MobileEndResponse(Constants.STR_ZERO, getCodeValue(Constants.STR_ZERO), body), response);
    }

    /**
     * 业务出现错误返回方法
     *
     * @param subCode  业务码
     * @param response 响应对象
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(Object subCode, HttpServletResponse response) {
        responseMessage(String.valueOf(subCode), getCodeValue(Constants.STR_ZERO), response);
    }


    /**
     * 业务出现错误返回方法
     *
     * @param subCode  业务码
     * @param subMsg   业务信息
     * @param response 响应对象
     * @author Mr.Yang
     * @date 2018/12/2
     */

    public static void responseMessage(String subCode, String subMsg, HttpServletResponse response) {
        //直接写出
        write(new MobileEndResponse(null, null, null, subCode, subMsg), response);
    }


    public MobileEndResponse() {
    }

    public MobileEndResponse(String code, String msg) {
        this(code, msg, null);
    }

    public MobileEndResponse(String code, String msg, String body) {
        this(code, msg, body, null, null);
    }

    public MobileEndResponse(String code, String msg, String body, String subCode, String subMsg) {
        super(code, msg, body, subCode, subMsg);
    }

}
