package com.semitransfer.common.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.semitransfer.common.api.response.DesktopEndResponse;
import com.semitransfer.common.util.AclUtils;
import com.semitransfer.common.util.StringUtils;
import com.semitransfer.plus.config.internal.ConfigConstants;
import com.semitransfer.plus.config.internal.util.redis.RedisTempalte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.semitransfer.common.api.BaseRquestAndResponse.getResponse;

/**
 * <p>
 * 权限拦截
 * </p>
 *
 * @author Mr.Yang
 * @since 2019-02-27
 */
@Component
public class AclRequestInterceptor implements Filter {

    private static Logger logger = LoggerFactory.getLogger(AclRequestInterceptor.class);

    /**
     * 字符串缓存
     */
    @Autowired
    private RedisTempalte redisTempalte;

    /**
     * 环境
     */
    @Autowired
    private Environment environment;

    @Override
    public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        //获取请求链接
        String requestLinke = request.getRequestURI().substring(StringUtils.isEmpty(
                environment.getProperty(Constants.FIELD_APPLICATION_NAME)) ? Constants.NUM_ZERO :
                Objects.requireNonNull(environment.getProperty(Constants.FIELD_APPLICATION_NAME)).length() + Constants.NUM_ONE);
        //不需要处理拦截
        if (this.redisTempalte.exists(ConfigConstants.NUM_ZERO, ConfigConstants.FIELD_ACL_INTERCEPTOR)) {
            //全部过滤路径
            String allAclInterceptor = this.redisTempalte.get(ConfigConstants.FIELD_ACL_INTERCEPTOR);
            //精确匹配路径 模糊匹配
            String path = "";
            try {
                path = "/".concat(requestLinke.split("/")[ConfigConstants.NUM_ONE]).concat("/*");
            } catch (Exception e) {
                DesktopEndResponse.responseMessage(99994, getResponse());
                return;
            }
            assert allAclInterceptor != null;
            if (allAclInterceptor.contains(requestLinke) || allAclInterceptor.contains(path)) {
                chain.doFilter(req, response);
                return;
            }
        }
        //获取请求参数
        JSONObject params = JSONObject.parseObject(request.getParameter(Constants.FIELD_PARAMS));
        //不包含权限码直接返回
        if (null == params || !params.containsKey(Constants.POWER_CODE)) {
            DesktopEndResponse.responseMessage(99995, getResponse());
            return;
        }
        //获取用户信息
        JSONObject userJSON = JSON.parseObject(this.redisTempalte
                .get(request.getHeader(Constants.FIELD_TOKEN)));
        //校验是否有权限此次接口
        String powerList = this.redisTempalte.hget(ConfigConstants.NUM_ZERO, Constants.POWER, userJSON.getString(Constants.FIELD_ID));
        assert powerList != null;
        if (!powerList.contains(params.getString(Constants.POWER_CODE))) {
            DesktopEndResponse.responseMessage(99997, getResponse());
            return;
        }
        //检索唯一授权码信息
        List<AclUtils> aclUtilsList = JSONArray.parseArray(powerList, AclUtils.class);
        aclUtilsList = aclUtilsList.stream().filter(
                item -> item.getAclCode().equals(params.getString(Constants.POWER_CODE))).collect(Collectors.toList());
        //如果为空则表示不存在授权信息
        if (aclUtilsList.isEmpty() || !request.getRequestURI().contains(aclUtilsList.get(Constants.NUM_ZERO).getUrl())) {
            DesktopEndResponse.responseMessage(99996, getResponse());
            return;
        }
        chain.doFilter(req, response);
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
}
