package com.semitransfer.plus.config.internal.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.semitransfer.plus.config.internal.ConfigConstants;
import com.semitransfer.plus.config.internal.ConfigEnum;
import com.semitransfer.plus.config.internal.NacosProperties;
import org.springframework.util.StringUtils;

import java.util.Properties;

import static com.semitransfer.plus.config.internal.util.ConvertHandler.appendMsgToProperties;
import static com.semitransfer.plus.config.internal.util.ConvertHandler.convertTableToEntity;

/**
 * nacos处理器
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-12-01 00:49
 * @version:2.0
 **/
public class NacosHandler {

    /**
     * 使用nacos配置来缓存code
     *
     * @param nacosProperties nacos配置参数
     * @return 返回处理信息
     * @author Mr.Yang
     * @date 2018/12/1
     */
    public static String nacosCacheCode(NacosProperties nacosProperties) {
        //获取nacos的配置信息
        JSONArray resultJson;
        //添加获取返回码固定值
        nacosProperties.setGroup(ConfigConstants.NACOS_DEFAULT_GROUP);
        nacosProperties.setDataId(ConfigConstants.FIELD_CACHE_CODE);
        String nacosResult = getNacosConfig(nacosProperties);
        try {
            //判断是否存在数据
            if (StringUtils.isEmpty(nacosResult)) {
                return ConfigEnum.NACOS_MISSING_DATA_ERROR.getKey();
            }
            resultJson = JSONArray.parseArray(nacosResult);
        } catch (Exception e) {
            return nacosResult;
        }
        //成功
        return appendMsgToProperties(resultJson) ? ConfigEnum.PROCESS_SUCCESS.getKey() : ConfigEnum.PROCESS_FAIL.getKey();
    }

    /**
     * 将项目所有的请求存放在配置中心
     *
     * @param nacosProperties 配置
     * @param urlValue        url参数
     * @author Mr.Yang
     * @date 2018/12/7 0007
     */
    public static void nacosRequestMapping(NacosProperties nacosProperties, String urlValue) {
        //更新至nacos中
        try {
            getConfigServiceInstance(nacosProperties).publishConfig(
                    //数据标记
                    StringUtils.isEmpty(nacosProperties.getDataId()) ? ConfigConstants.URL_LIST
                            : nacosProperties.getDataId(),
                    //分组
                    StringUtils.isEmpty(nacosProperties.getGroup()) ? ConfigConstants.NACOS_DEFAULT_GROUP
                            : nacosProperties.getGroup(), urlValue);
            //处理过滤
            getConfigServiceInstance(nacosProperties).publishConfig(
                    //数据标记
                    StringUtils.isEmpty(nacosProperties.getDataId()) ? ConfigConstants.FIELD_ACL_INTERCEPTOR
                            : nacosProperties.getDataId(),
                    //分组
                    StringUtils.isEmpty(nacosProperties.getGroup()) ? ConfigConstants.NACOS_DEFAULT_GROUP
                            : nacosProperties.getGroup(), urlValue);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用nacos配置来进行转换
     *
     * @param nacosProperties nacos配置参数
     * @return 返回处理信息
     * @author Mr.Yang
     * @date 2018/12/1
     */
    public static String nacosConvert(NacosProperties nacosProperties) {
        //获取nacos的配置信息
        JSONArray resultJson;
        String nacosResult = getNacosConfig(nacosProperties);
        try {
            //判断是否存在数据
            if (StringUtils.isEmpty(nacosResult)) {
                return ConfigEnum.NACOS_MISSING_DATA_ERROR.getKey();
            }
            resultJson = JSONArray.parseArray(nacosResult);
            //解析配置信息并开始循环转换实体
            JSONArray configArray = new JSONArray();
            resultJson.forEach(config -> {
                //从新赋值
                String tempConfig = String.valueOf(config);
                //转换json格式
                JSONObject tempJsonConfig = JSONObject.parseObject(tempConfig);
                //开始处理生成 不能存在状态字段，在上一次成功转换后重新标记的字段
                if (!tempJsonConfig.containsKey(ConfigConstants.FIELD_CONVERT_STATUS) &&
                        convertTableToEntity(tempConfig)) {
                    //处理成功新增一个状态
                    tempJsonConfig.put(ConfigConstants.FIELD_CONVERT_STATUS, 0);
                }
                configArray.add(tempJsonConfig);
            });
            //更新至nacos中
            boolean isPublishOk = getConfigServiceInstance(nacosProperties).publishConfig(nacosProperties.getDataId(),
                    nacosProperties.getGroup(), configArray.toJSONString());
            //是否发布成功
            return isPublishOk ? ConfigEnum.NACOS_PROCESS_SUCCESS.getKey()
                    : ConfigEnum.NACOS_PULISH_CONFIG_ERROR.getKey();
        } catch (Exception e) {
            return nacosResult;
        }
    }


    /**
     * 从nacos配置中心获取生成表信息
     *
     * @param nacosProperties nacos配置参数
     * @return 配置中心查找到信息
     * @author Mr.Yang
     * @date 2018/11/30
     */
    private static String getNacosConfig(NacosProperties nacosProperties) {
        try {
            // 获取配置信息
            return getConfigServiceInstance(nacosProperties).getConfig(
                    nacosProperties.getDataId(), nacosProperties.getGroup(),
                    nacosProperties.getTime());
        } catch (NacosException e) {
            return e.getMessage();
        }
    }

    /**
     * 获取nacos配置实例
     *
     * @param nacosProperties nacos配置参数
     * @return 返回nacos实例
     * @throws NacosException nacos异常
     * @author Mr.Yang
     * @date 2018/12/1
     */
    private static ConfigService getConfigServiceInstance(NacosProperties nacosProperties) throws NacosException {
        //配置信息
        Properties properties = new Properties();
        properties.put(ConfigConstants.FIELD_SERVER_ADDR, nacosProperties.getServerAddr());
        return NacosFactory.createConfigService(properties);
    }
}
