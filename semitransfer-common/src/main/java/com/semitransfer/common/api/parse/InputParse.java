package com.semitransfer.common.api.parse;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.page.Input;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.util.StringUtils;

import java.lang.reflect.Field;

/**
 * <p>
 * Input组件解析
 * </p>
 *
 * @author Mr.Yang
 * @since 2018-12-18
 */
public class InputParse extends ParseBase {

    @Override
    public JSONObject parse(Field field, String options) {
        return parse(field, options, false);
    }

    @Override
    public JSONObject parse(Field field, String options, boolean flag) {
        //处理责任注解
        Input inputField = field.getAnnotation(Input.class);
        //组件配置
        JSONObject nodeConfig = new JSONObject();
        //返回结果
        JSONObject result = new JSONObject();
        //组件内置信息
        JSONObject props = new JSONObject();
        //提示
        props.put(Constants.FIELD_PLACEHOLDER, StringUtils.notEmptyEnhance(inputField.placeholder()) ?
                inputField.placeholder() : null);
        //类型
        props.put(Constants.FIELD_TYPE, StringUtils.notEmptyEnhance(inputField.type()) ? inputField.type() : "text");
        if (!options.startsWith(Constants.FIELD_SEARCH.toUpperCase())) {
            //输入框尺寸
            props.put(Constants.FIELD_SIZE, StringUtils.notEmptyEnhance(inputField.size()) ? inputField.size() : null);
            //	设置输入框为只读
            props.put(Constants.FIELD_READONLY, inputField.readonly());
            //是否可清空
            props.put(Constants.FIELD_CLEARABLE, inputField.clearable());
            ///是否禁用
            props.put(Constants.FIELD_DISABLED, inputField.disabled());
            //是否支持搜索
            props.put(Constants.FIELD_SEARCH, inputField.search());
            //文本
            if (Constants.FIELD_TEXTAREA.equals(props.getString(Constants.FIELD_TYPE))) {
                props.put(Constants.FIELD_ROWS, inputField.rows() == 0 ? 2 : inputField.rows());
                props.put(Constants.FIELD_AUTOSIZE, StringUtils.notEmptyEnhance(inputField.autosize()) ? inputField.autosize() : null);
                props.put(Constants.FIELD_WRAP, StringUtils.notEmptyEnhance(inputField.wrap()) ? inputField.wrap() : null);
            }
            //正则校验
            result.put(Constants.FIELD_RULES, inputField.rules());
        }
        //实例数据
        nodeConfig.put(Constants.FIELD_PROPS, props);
        //组件配置
        result.put(Constants.FIELD_NODE_CONFIG, nodeConfig);
        return result;
    }
}
