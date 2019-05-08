package com.semitransfer.plus.config.internal;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.nacos.client.config.NacosConfigService;
import com.baomidou.mybatisplus.annotation.EntityField;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.semitransfer.plus.config.internal.annotation.AclManage;
import com.semitransfer.plus.config.internal.util.redis.RedisTempalte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.semitransfer.plus.config.internal.util.NacosHandler.nacosRequestMapping;
import static com.semitransfer.plus.config.internal.util.RedisHandler.redisRequestMapping;

/**
 * 通用注入
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-12-01 11:54
 * @version:2.0
 **/
@Configuration
public class GeneralInjection {

    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(GeneralInjection.class);

    @Autowired
    WebApplicationContext applicationContext;

    /**
     * 环境
     */
    @Autowired
    Environment environment;

    /**
     * jedis
     */
    @Autowired
    RedisTempalte redisTempalte;

    /**
     * 自定义nacos配置
     */
    @Autowired
    NacosProperties nacosProperties;

    /**
     * 初始化检索所有requestMapping
     *
     * @author Mr.Yang
     * @date 2018/12/7 0007
     */
    @PostConstruct
    public void retrievalRequestMapping() throws InterruptedException {
        //新增免过滤路径
        String interceptroList = StringUtils.isEmpty(this.environment.getProperty(ConfigConstants.FIELD_ACL_INTERCEPTOR)) ?
                ConfigConstants.ACL_INTERCEPTOR_LIST.substring(ConfigConstants.NUM_ONE) :
                this.environment.getProperty(ConfigConstants.FIELD_ACL_INTERCEPTOR).concat(ConfigConstants.ACL_INTERCEPTOR_LIST);
        //获取使用RequestMapping注解方法
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        // 获取url与类和方法的对应信息
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        //获取所有url链接
        List<Map<String, String>> listUrl = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> m : map.entrySet()) {
            boolean flag = true;
            Map<String, String> methodMap = new HashMap<>(ConfigConstants.NUM_SIX);
            RequestMappingInfo info = m.getKey();
            HandlerMethod method = m.getValue();
            // 方法名
            String methodName = method.getMethod().getName();
            //过滤不展示处理的方法
            String[] excludeUlr = {"tableToEntity", "processCacheCode", "error"};
            for (String url : excludeUlr) {
                //如果开头以过滤的，则默认不展示
                if (methodName.startsWith(url)) {
                    flag = false;
                    break;
                }
            }
            //不是过滤数组则可以加入展示
            if (flag) {
                PatternsRequestCondition p = info.getPatternsCondition();
                methodMap.put(ConfigConstants.FIELD_METHOD, methodName);
                // 一个方法可能对应多个url
                methodMap.put(ConfigConstants.FIELD_URL, JSONArray.toJSONString(p.getPatterns()));
                // 类名
                methodMap.put(ConfigConstants.FIELD_CLASS_NAME, method.getMethod().getDeclaringClass().getName());
                //实体类
                try {
                    Class<?> clazz = Class.forName(method.getMethod().getDeclaringClass().getName());
                    //是否存在注解
                    if (clazz.isAnnotationPresent(EntityField.class)) {
                        methodMap.put(ConfigConstants.FIELD_ENTITY_CLASS,
                                clazz.getAnnotation(EntityField.class).value().getName());
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                //反射获取自定义注解
                AclManage loginManage = method.getMethodAnnotation(AclManage.class);
                //获取名称
                if (loginManage != null) {
                    methodMap.put(ConfigConstants.FIELD_ACL_NAME, loginManage.value());
                }
                RequestMethodsRequestCondition methodsCondition = info.getMethodsCondition();
                for (RequestMethod requestMethod : methodsCondition.getMethods()) {
                    methodMap.put(ConfigConstants.FIELD_TEPY, requestMethod.toString());
                }
                //权限码
                String aclCode = String.valueOf(System.currentTimeMillis());
                TimeUnit.MILLISECONDS.sleep(50);
                //开头1新增 2删除 3更新 4列表/检索 5导出 6导入 7跳转 8详情 9预新增
                switch (methodName) {
                    case ConfigConstants.FIELD_SAVE:
                        aclCode = String.valueOf(ConfigConstants.NUM_ONE).concat(aclCode);
                        break;
                    case ConfigConstants.FIELD_REMOVE:
                        aclCode = String.valueOf(ConfigConstants.NUM_TWO).concat(aclCode);
                        break;
                    case ConfigConstants.FIELD_UPDATE:
                        aclCode = String.valueOf(ConfigConstants.NUM_THREE).concat(aclCode);
                        break;
                    case ConfigConstants.FIELD_LIST:
                        aclCode = String.valueOf(ConfigConstants.NUM_FOUR).concat(aclCode);
                        break;
                    case ConfigConstants.FIELD_EXPORT:
                        aclCode = String.valueOf(ConfigConstants.NUM_FIVE).concat(aclCode);
                        break;
                    case ConfigConstants.FIELD_IMPORT:
                        aclCode = String.valueOf(ConfigConstants.NUM_SIX).concat(aclCode);
                        break;
                    case ConfigConstants.FIELD_JUMP:
                        aclCode = String.valueOf(ConfigConstants.NUM_SEVEN).concat(aclCode);
                        break;
                    case ConfigConstants.FIELD_GET:
                        aclCode = String.valueOf(ConfigConstants.NUM_EIGHT).concat(aclCode);
                        break;
                    case ConfigConstants.FIELD_PRE_ADDED:
                        aclCode = String.valueOf(ConfigConstants.NUM_NINE).concat(aclCode);
                        break;
                    default:
                        aclCode = String.valueOf(ConfigConstants.NUM_ZERO).concat(aclCode);
                        break;
                }
                methodMap.put(ConfigConstants.FIELD_ACL_CODE, aclCode);
                listUrl.add(methodMap);
            }
        }
        boolean checkClass;
        //寻找是否存在redis依赖
        checkClass = ClassUtils.isPresent(ConfigConstants.REDIS_PATH,
                JedisPoolConfig.class.getClassLoader());
        try {
            //有依赖并且已经存在连接信息
            if (checkClass) {
                redisRequestMapping(redisTempalte, JSONArray.toJSONString(listUrl));
                //压入免过滤信息
                this.redisTempalte.set(ConfigConstants.FIELD_ACL_INTERCEPTOR, interceptroList);
                return;
            }
        } catch (Exception e) {
            logger.warn("Found Redis dependency, configuration information does not exist. Continue execution");
        }
        //以上为redis完成全部校验信息
        //========================================================//
        //========================================================//
        //========================================================//
        //========================================================//
        //寻找是否存在nacos依赖
        try {
            checkClass = ClassUtils.isPresent(ConfigConstants.NACOS_PATH,
                    NacosConfigService.class.getClassLoader());
            //验证配置是否齐全
            if (StringUtils.isEmpty(nacosProperties.getServerAddr()) && checkClass) {
                //返回缺失配置提示
                logger.warn(ConfigEnum.NACOS_MISSING_CONFIG_ERROR.getKey());
            } else if (!StringUtils.isEmpty(nacosProperties.getDataId()) && !checkClass) {
                //返回缺少nacos依赖
                logger.warn(ConfigEnum.NACOS_MISSING_DEPENDENCY_ERROR.getKey());
            } else if (checkClass) {
                nacosRequestMapping(nacosProperties, JSONArray.toJSONString(listUrl));
            }
        } catch (Exception e) {
            logger.warn("Found Nacos dependency, configuration information does not exist. Continue execution");
        }
        //以上为nacos完成全部校验信息
        //========================================================//
        //========================================================//
        //========================================================//
        //========================================================//

    }


    /**
     * mybatis-plus分页插件
     *
     * @author Mr.Yang
     * @date 2018/12/3
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
