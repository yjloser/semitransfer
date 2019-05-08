package com.semitransfer.plus.config;

import com.alibaba.nacos.client.config.NacosConfigService;
import com.semitransfer.plus.config.internal.ConfigConstants;
import com.semitransfer.plus.config.internal.ConfigEnum;
import com.semitransfer.plus.config.internal.NacosProperties;
import com.semitransfer.plus.config.internal.util.redis.RedisTempalte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPoolConfig;

import static com.semitransfer.plus.config.internal.util.ConvertHandler.cacheCode;
import static com.semitransfer.plus.config.internal.util.ConvertHandler.convertTableToEntity;
import static com.semitransfer.plus.config.internal.util.NacosHandler.nacosCacheCode;
import static com.semitransfer.plus.config.internal.util.NacosHandler.nacosConvert;
import static com.semitransfer.plus.config.internal.util.RedisHandler.redisCacheCode;
import static com.semitransfer.plus.config.internal.util.RedisHandler.redisConvert;


/**
 * 配置信息控制中心
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-11-29 14:22
 * @version:2.0
 **/
@RestController
@RequestMapping(ConfigConstants.CONFIG_INDEX)
public class ConfigController {

    /**
     * 自定义nacos配置
     */
    @Autowired
    NacosProperties nacosProperties;

    /**
     * jedis
     */
    @Autowired
    RedisTempalte redisTempalte;

    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(ConfigController.class);

    /**
     * 从数据库表转换为实体等（service、entity、controller、mapper）
     *
     * @param convert 请求的参数 json格式
     * @return 返回是否创建成功
     * @author Mr.Yang
     * @date 2018/11/29 0029
     */
    @RequestMapping(ConfigConstants.TABLE_TO_ENTITY)
    public String tableToEntity(@RequestParam(required = false) String convert) {
        //如果传入的字段不为空，则表示不是通过接口传值
        if (!StringUtils.isEmpty(convert)) {
            //处理自动转换工具
            return convertTableToEntity(convert) ? ConfigEnum.PROCESS_SUCCESS.getKey()
                    : ConfigEnum.CONVERT_ERROR.getKey();
        }
        boolean checkClass;
        try {
            //寻找是否存在redis依赖
            checkClass = ClassUtils.isPresent(ConfigConstants.REDIS_PATH,
                    JedisPoolConfig.class.getClassLoader());
            //有依赖并且已经存在连接信息
            if (checkClass) {
                return redisConvert(redisTempalte);
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
                return ConfigEnum.NACOS_MISSING_CONFIG_ERROR.getKey();
            } else if (!StringUtils.isEmpty(nacosProperties.getDataId()) && !checkClass) {
                //返回缺少nacos依赖
                return ConfigEnum.NACOS_MISSING_DEPENDENCY_ERROR.getKey();
            } else if (checkClass) {
                return nacosConvert(nacosProperties);
            }
        } catch (Exception e) {
            logger.warn("Found Nacos dependency, configuration information does not exist. Continue execution");
        }
        //以上为nacos完成全部校验信息
        //========================================================//
        //========================================================//
        //========================================================//
        //========================================================//
        return null;
    }


    /**
     * 缓存code信息
     *
     * @param codes code信息
     * @return 返回是否创建成功
     * @author Mr.Yang
     * @date 2018/11/29 0029
     */
    @RequestMapping(ConfigConstants.PROCESS_CACHE_CODE)
    public String processCacheCode(@RequestParam(required = false) String codes) {
        //如果传入的字段不为空，则表示不是通过接口传值
        if (!StringUtils.isEmpty(codes)) {
            //处理自动转换工具
            return cacheCode(codes) ? ConfigEnum.PROCESS_SUCCESS.getKey()
                    : ConfigEnum.PROCESS_FAIL.getKey();
        }
        boolean checkClass;
        try {
            //寻找是否存在redis依赖
            checkClass = ClassUtils.isPresent(ConfigConstants.REDIS_PATH,
                    JedisPoolConfig.class.getClassLoader());
            //有依赖并且已经存在连接信息
            if (checkClass) {
                return redisCacheCode(redisTempalte);
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
            if (!checkClass) {
                //返回缺少nacos依赖
                return ConfigEnum.NACOS_MISSING_DEPENDENCY_ERROR.getKey();
            } else if (StringUtils.isEmpty(nacosProperties.getServerAddr())) {
                //返回缺失配置提示
                return ConfigEnum.NACOS_MISSING_CONFIG_ERROR.getKey();
            }
            return nacosCacheCode(nacosProperties);
        } catch (Exception e) {
            logger.warn("Found Nacos dependency, configuration information does not exist. Continue execution");
        }
        //以上为nacos完成全部校验信息
        return null;
    }
}
