package com.semitransfer.common.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.encrypt.AnalyzeUtils;

import java.lang.reflect.Field;

/**
 * <p>
 * 解析基类
 * </p>
 *
 * @author Mr.Yang
 * @since 2018-12-18
 */
public class ParseBase {

    /**
     * 解析自定义注解
     *
     * @param options 操作
     * @param field   自定义注解
     * @return 返回解析结果
     * @author Mr.Yang
     * @date 2018/12/18 0018
     */
    public JSONObject parse(Field field, String options) {
        return parse(field, options, false);
    }

    /**
     * 解析自定义注解
     *
     * @param field   自定义注解
     * @param options 操作
     * @param flag    是否拉取预准备数据
     * @return 返回解析结果
     * @author Mr.Yang
     * @date 2018/12/18 0018
     */
    public JSONObject parse(Field field, String options, boolean flag) {
        return null;
    }

    /**
     * 解析检索条件
     *
     * @param key    key查询
     * @param result 返回信息
     * @return 返回解析结果
     * @author Mr.Yang
     * @date 2018/12/18 0018
     */
    public JSONObject searchValue(String key, JSONObject result) {
        return searchValue(key, result, false);
    }


    /**
     * 解析检索条件
     *
     * @param key     key查询
     * @param result  返回信息
     * @param refresh true不从map中获取 默认false
     * @return 返回解析结果
     * @author Mr.Yang
     * @date 2018/12/18 0018
     */
    public JSONObject searchValue(String key, JSONObject result, boolean refresh) {
        String tempResult = String.valueOf(AnalyzeUtils.searchMap.get(key));
        if (tempResult.startsWith("[{")) {
            //处理查询
            result.put(Constants.FIELD_DATA, JSON.parseArray(tempResult));
        } else if (tempResult.startsWith("{")) {
            //处理查询
            result.put(Constants.FIELD_DATA, JSON.parseObject(tempResult));
        } else {
            result.put(Constants.FIELD_DATA, tempResult);
        }
        return result;
    }
}
