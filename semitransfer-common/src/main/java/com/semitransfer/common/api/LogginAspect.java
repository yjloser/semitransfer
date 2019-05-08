package com.semitransfer.common.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.semitransfer.common.api.response.DesktopEndResponse;
import com.semitransfer.common.encrypt.DesECBUtils;
import com.semitransfer.common.util.StringUtils;
import com.semitransfer.plus.config.internal.ConfigConstants;
import com.semitransfer.plus.config.internal.annotation.LoginManage;
import com.semitransfer.plus.config.internal.util.redis.RedisTempalte;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.semitransfer.common.api.BaseRquestAndResponse.getRequest;
import static com.semitransfer.common.api.BaseRquestAndResponse.getResponse;
import static com.semitransfer.common.encrypt.RsaUtils.loadPrivateKey;
import static com.semitransfer.common.encrypt.RsaUtils.loadPublicKey;

/**
 * <p>
 * 拦截登录
 * <p>
 *
 * @author: Mr.Yang
 * @date: 2018-12-09 09:47
 **/
@Aspect
@Component
@Order(2)
public class LogginAspect {

    private static Logger logger = LoggerFactory.getLogger(LogginAspect.class);

    /**
     * 字符串缓存
     */
    @Autowired
    private RedisTempalte redisTempalte;

    /**
     * 公钥
     */
    private RSAPublicKey publicKey;
    /**
     * 私钥
     */
    private RSAPrivateKey privateKey;

    /**
     * 环境
     */
    @Autowired
    private Environment environment;

    /**
     * 初始化加密
     */
    public static Algorithm algorithm;

    {
        try {
            //私钥
            privateKey = loadPrivateKey("MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALY49wZPNXST0ZEi\n" +
                    "G60/FrjDD8do8OJGIjSPSIKvhX5AbW6uz8mL0wYrqOm4L2jsgmlMaGQ3uPyC7RxN\n" +
                    "qui3CGktSSFOoklFp7qFdHOUH3dbBPRoIV2H4ALCJHT22JD1ZZJVf4v1o/goF5TP\n" +
                    "yaNTE7VieJFTBgxxOEWXqc7f6fwlAgMBAAECgYEArAP2XQRk59mqmSDjk5Xcsymg\n" +
                    "OZP84P1nyMoBnyxmDhpvs25eRFo0KL0KRSdTye6J5TD10rUvcV9+yZsf3XL7AgQY\n" +
                    "WpAuR9TYYxzIl0hIOiTLeBp/qHiDWwc1CaYPbnPgdHNHUHLaWCA+h/OOrtsUP40C\n" +
                    "hh6SJHg7hs2XOOkvUnkCQQDxxJxlFrJsxpiNKIeOGz6SM32e25eevY0VI9CTMIjk\n" +
                    "SaMEquFXQilVgr9CuGkRLEz1pYNzY5qONab6/AB8y4Z/AkEAwPMEV9oYSWPcG5Yh\n" +
                    "1IMEEhhYBDCmGwGW6OD1ULVP8kLKACZCnTY9PwS5Z3eO2M+z9nhBCgw3KtZUp0v/\n" +
                    "ugpTWwJANg8eYUQn9UaaycVsOgxBe3Nj/WdgibAcocN2WdMaaOFGQD7tUBONJn+r\n" +
                    "wIF3jM15D9xIfj6hSncYtTov6beghQJAUSYj/nrYWg1opiWHRuRvUtjwM5ruUU9i\n" +
                    "08DBC9elrwMOB/APdiU4rwdinrR23JLGYnODDyHCFf8cjVv2Sp1LHwJAM1CLXPRn\n" +
                    "MTiEEadFXAg6SB7D0jNdCre3g42bSR0DrSPHvLNWd+i7NXUrLvO/hy9iVwNYQwJh\n" +
                    "JXiLcRLndttb8g==");
            //公钥
            publicKey = loadPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC2OPcGTzV0k9GRIhutPxa4ww/H\n" +
                    "aPDiRiI0j0iCr4V+QG1urs/Ji9MGK6jpuC9o7IJpTGhkN7j8gu0cTarotwhpLUkh\n" +
                    "TqJJRae6hXRzlB93WwT0aCFdh+ACwiR09tiQ9WWSVX+L9aP4KBeUz8mjUxO1YniR\n" +
                    "UwYMcThFl6nO3+n8JQIDAQAB");
            algorithm = Algorithm.RSA256(publicKey, privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 前置登录处理
     *
     * @param loginManag 拦截注解
     * @author Mr.Yang
     * @date 2018/12/10
     */
    @Before("@annotation(loginManag)")
    public void beforeLogin(LoginManage loginManag) {
        //获取请求和响应
        HttpServletRequest request = getRequest();
        //获取请求链接
        String requestLinke = request.getRequestURI();
        //非过滤标记位
        boolean isAspect = true;
        //获取请求链接
        String tempRequestLinke = requestLinke.substring(StringUtils.isEmpty(
                environment.getProperty(Constants.FIELD_APPLICATION_NAME)) ? Constants.NUM_ZERO :
                Objects.requireNonNull(environment.getProperty(Constants.FIELD_APPLICATION_NAME)).length() + Constants.NUM_ONE);
        //不需要处理拦截
        if (this.redisTempalte.exists(ConfigConstants.NUM_ZERO, ConfigConstants.FIELD_ACL_INTERCEPTOR)) {
            //全部过滤路径
            String allAclInterceptor = this.redisTempalte.get(ConfigConstants.FIELD_ACL_INTERCEPTOR);
            //精确匹配路径 模糊匹配
            String path = "";
            try {
                path = "/".concat(tempRequestLinke.split("/")[ConfigConstants.NUM_ONE]).concat("/*");
            } catch (Exception e) {
                DesktopEndResponse.responseMessage(99994, getResponse());
                return;
            }
            assert allAclInterceptor != null;
            if (allAclInterceptor.contains(tempRequestLinke) || allAclInterceptor.contains(path)) {
                isAspect = false;
            }
        }
        //过滤器
        String[] excludes = {Constants.FIELD_LOGIN, Constants.FIELD_REFRESH};
        //标记
        boolean flag = true;
        for (String exclude : excludes) {
            //包含处理
            if (requestLinke.contains(exclude)) {
                flag = false;
                break;
            }
        }
        //如果第一次拦截为false,则跳过第二次拦截
        if (!isAspect) {
            flag = false;
        }
        //为真才进去校验
        if (flag || isAspect) {
            try {
                //获取校验
                DecodedJWT jwt = deToken(request.getHeader(Constants.FIELD_AUTHORIZATION));
                if (jwt != null) {
                    JSONObject tokenJson = JSONObject.parseObject(Objects.requireNonNull(jwt).getSubject());
                    //获取登录状态
                    if (!this.redisTempalte.exists(ConfigConstants.NUM_ZERO, tokenJson.getString(Constants.FIELD_LOGIN_KEY)) ||
                            !this.redisTempalte
                                    .hexists(ConfigConstants.NUM_ZERO, Constants.MENU,
                                            JSON.parseObject(this.redisTempalte.get(
                                                    tokenJson.getString(Constants.FIELD_LOGIN_KEY))).getString(Constants.FIELD_ID))) {
                        DesktopEndResponse.responseMessage(99999, getResponse());
                        //删除当前使用的token信息
                        this.redisTempalte.del(ConfigConstants.NUM_ZERO, tokenJson.getString(Constants.FIELD_TOKEN));
                        return;
                    }
                    //获取token
                    if (StringUtils.isEmptyEnhance(request.getHeader(Constants.FIELD_TOKEN)) ||
                            !this.redisTempalte.exists(ConfigConstants.NUM_ZERO, tokenJson.getString(Constants.FIELD_TOKEN))) {
                        DesktopEndResponse.responseMessage(99998, getResponse());
                        return;
                    }
                    //获取用户信息
                    JSONObject userJSON = JSON.parseObject(this.redisTempalte.get(request.getHeader(Constants.FIELD_TOKEN)));
                    //压入用户名
                    request.setAttribute(Constants.FIELD_OPERATOR_NAME,
                            userJSON.getString(Constants.FIELD_CONTACTS_NAME));
                    //续约30分钟
                    this.redisTempalte.expire(tokenJson.getString(Constants.FIELD_LOGIN_KEY), 1800);
                    //续约40分钟
                    this.redisTempalte.expire(tokenJson.getString(Constants.FIELD_TOKEN), 2400);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //非法鉴权
                DesktopEndResponse.responseMessage(99998, getResponse());
            }
        }
    }


    /**
     * 返回前登录处理
     *
     * @param loginManag 拦截注解
     * @author Mr.Yang
     * @date 2018/12/10
     */
    //@AfterReturning(value = "@annotation(loginManag)", returning = "result")
    public void afterLogin(LoginManage loginManag, Object result) {
        appendFailOrSuccess(result);
    }


    /**
     * 异常处理
     *
     * @param loginManag 拦截注解
     * @author Mr.Yang
     * @date 2018/12/10
     */
    //@AfterThrowing("@annotation(loginManag)")
    public void afterLoginThrows(LoginManage loginManag) {
        appendFailOrSuccess(DesktopEndResponse.responseMessage(Constants.NUM_ONE));
    }

    /**
     * 业务处理异常或成功
     *
     * @param result 返回值
     * @author Mr.Yang
     * @date 2018/12/9
     */
    private void appendFailOrSuccess(Object result) {
        HttpServletResponse response = getResponse();
        response.setHeader("Access-Control-Expose-Headers", "*");
        //创建payload的私有声明（根据特定的业务需要添加，如果要拿这个做验证，一般是需要和jwt的接收方提前沟通好验证方式的）
        Map<String, Object> claims = new HashMap<>(4);
        //转换
        JSONObject resultJson = JSONObject.parseObject(JSONObject.toJSONString(result));
        //获取用户loginKey
        String loginKey = null;
        //返回值中获取
        if (resultJson.containsKey(Constants.FIELD_LOGIN_KEY)) {
            loginKey = resultJson.getString(Constants.FIELD_LOGIN_KEY);
            //删除返回值中的loginKey
            resultJson.remove(Constants.FIELD_LOGIN_KEY);
        }
        //从请求头获取
        if (StringUtils.notEmpty(getRequest().getHeader(Constants.FIELD_TOKEN))) {
            loginKey = getRequest().getHeader(Constants.FIELD_TOKEN);
        }
        claims.put(Constants.FIELD_LOGIN_KEY, null == loginKey ? "" : loginKey);
        //生成token
        String token = null;
        try {
            token = DesECBUtils.encryptDES(String.valueOf(System.currentTimeMillis()), Constants.DES_PASWD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        claims.put(Constants.FIELD_TOKEN, token);
        //设置超时时间40分钟
        this.redisTempalte.set(token, token);
        this.redisTempalte.expire(token, 2400);
        //创建jwt
        String authorization = JWT.create()
                .withIssuer(Constants.ISS_UER).withSubject(JSON.toJSONString(claims))
                .sign(algorithm);
        //设置鉴权
        response.setHeader(Constants.FIELD_AUTHORIZATION, authorization);
        response.setHeader(Constants.FIELD_TOKEN, loginKey);
    }

    /**
     * 先验证token是否被伪造，然后解码token。
     *
     * @param token 字符串token
     * @return 解密后的DecodedJWT对象，可以读取token中的数据。
     * @author Mr.Yang
     * @date 2018/12/9
     */
    private DecodedJWT deToken(final String token) {
        try {
            //初始化验签
            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(Constants.ISS_UER)
                    .build();
            //验签
            return verifier.verify(token);
        } catch (JWTVerificationException | IllegalArgumentException exception) {
            exception.printStackTrace();
            return null;
        }
    }
}