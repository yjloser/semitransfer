package com.semitransfer.plus.config.internal.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.semitransfer.plus.config.internal.ConfigConstants;
import com.semitransfer.plus.config.internal.ConfigEnum;
import com.semitransfer.plus.config.internal.util.redis.RedisTempalte;
import org.springframework.util.StringUtils;

import static com.semitransfer.plus.config.internal.util.ConvertHandler.appendMsgToProperties;
import static com.semitransfer.plus.config.internal.util.ConvertHandler.convertTableToEntity;

/**
 * 缓存处理器
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-12-01 00:49
 * @version:2.0
 **/
public class RedisHandler {


    /**
     * 使用redis缓存获取code
     *
     * @param redisTempalte redis缓存
     * @return 返回处理信息
     * @author Mr.Yang
     * @date 2018/12/1
     */
    public static String redisCacheCode(RedisTempalte redisTempalte) {
        //获取nacos的配置信息
        JSONArray resultJson;
        try {
            //是否存在key
            if (!redisTempalte.exists(ConfigConstants.FIELD_CACHE_CODE)) {
                return ConfigEnum.REDIS_NOT_EXIST_RESPONSE_CODE.getKey();
            }
            //获取缓存中对应的值
            String value = redisTempalte.get(ConfigConstants.FIELD_CACHE_CODE);
            if (StringUtils.isEmpty(value)) {
                return ConfigEnum.REDIS_IN_KEY_RESPONSE_CODE_EMPTY.getKey();
            }
            //防止数据异常
            resultJson = JSONArray.parseArray(value);
        } catch (Exception e) {
            return ConfigEnum.CURRENT_NOT_IN_JSONARRAY.getKey();
        }
        //成功
        return appendMsgToProperties(resultJson) ? ConfigEnum.PROCESS_SUCCESS.getKey() : ConfigEnum.PROCESS_FAIL.getKey();
    }

    /**
     * 使用redis缓存
     *
     * @param redisTempalte redis缓存
     * @param urlValue      url参数
     * @author Mr.Yang
     * @date 2018/12/1
     */
    public static void redisRequestMapping(RedisTempalte redisTempalte, String urlValue) {
        //更新至redis中
        redisTempalte.set(ConfigConstants.URL_LIST, urlValue);
    }

    /**
     * 使用redis缓存来进行转换
     *
     * @param redisTempalte redis缓存
     * @return 返回处理信息
     * @author Mr.Yang
     * @date 2018/12/1
     */
    public static String redisConvert(RedisTempalte redisTempalte) {
        //获取nacos的配置信息
        JSONArray resultJson;
        try {
            //是否存在key
            if (!redisTempalte.exists(ConfigConstants.FIELD_TABLE_TO_ENTITY)) {
                return ConfigEnum.REDIS_NOT_EXIST_TABLE_TO_ENTITY.getKey();
            }
            //获取缓存中对应的值
            String value = redisTempalte.get(ConfigConstants.FIELD_TABLE_TO_ENTITY);
            if (StringUtils.isEmpty(value)) {
                return ConfigEnum.REDIS_IN_KEY_TABLE_TO_ENTITY_EMPTY.getKey();
            }
            //防止数据异常
            resultJson = JSONArray.parseArray(value);
        } catch (Exception e) {
            return ConfigEnum.CURRENT_NOT_IN_JSONARRAY.getKey();
        }
        //解析配置信息并开始循环转换实体
        JSONArray configArray = new JSONArray();
        assert resultJson != null;
        resultJson.forEach(config -> {
            //转换json格式
            JSONObject tempJsonConfig = JSONObject.parseObject(String.valueOf(config));
            //开始处理生成 不能存在状态字段，在上一次成功转换后重新标记的字段
            if (!tempJsonConfig.containsKey(ConfigConstants.FIELD_CONVERT_STATUS) &&
                    convertTableToEntity(String.valueOf(config))) {
                //处理成功新增一个状态
                tempJsonConfig.put(ConfigConstants.FIELD_CONVERT_STATUS, 0);
            }
            configArray.add(tempJsonConfig);
        });
        //更新至redis中
        redisTempalte.set(
                ConfigConstants.FIELD_TABLE_TO_ENTITY, configArray.toJSONString());
        //成功
        return ConfigEnum.PROCESS_SUCCESS.getKey();
    }
}
