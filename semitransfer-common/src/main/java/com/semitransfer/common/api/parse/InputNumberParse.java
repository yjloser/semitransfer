package com.semitransfer.common.api.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.page.InputNumber;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.util.StringUtils;

import java.lang.reflect.Field;

/**
 * <p>
 * 解析InputNumber组件
 * </p>
 *
 * @author Mr.Yang
 * @since 2018-12-19
 */
public class InputNumberParse extends ParseBase {

    @Override
    public JSONObject parse(Field field, String options) {
        return parse(field, options, false);
    }

    @Override
    public JSONObject parse(Field field, String options, boolean flag) {
        //处理责任注解
        InputNumber inputNumber = field.getAnnotation(InputNumber.class);
        //返回结果
        JSONObject result = new JSONObject();
        //组件配置
        JSONObject nodeConfig = new JSONObject();
        //组件内置信息
        JSONObject props = new JSONObject();
        //占位文本
        props.put(Constants.FIELD_PLACEHOLDER, StringUtils.notEmptyEnhance(inputNumber.placeholder()) ?
                inputNumber.placeholder() : null);
        //最大值
        props.put(Constants.FIELD_MAX, inputNumber.max() == 0 ? inputNumber.max() : null);
        //最小值
        props.put(Constants.FIELD_MIN, inputNumber.min() == 0 ? inputNumber.min() : null);
        //最小值
        props.put(Constants.FIELD_STEP, inputNumber.step() == 0 ? inputNumber.step() : null);
        //输入框尺寸
        props.put(Constants.FIELD_SIZE, StringUtils.notEmptyEnhance(inputNumber.size()) ? inputNumber.size() : null);
        //	设置输入框为只读
        props.put(Constants.FIELD_READONLY, inputNumber.readonly());
        //是否可编辑
        props.put(Constants.FIELD_EDITABLE, inputNumber.editable());
        //数值精度
        props.put(Constants.FIELD_PRECISION, inputNumber.precision() == 0 ? inputNumber.precision() : null);
        //默认值
        props.put(Constants.FIELD_VALUE, inputNumber.value() == 0 ? inputNumber.value() : null);
        //是否禁用
        props.put(Constants.FIELD_DISABLED, inputNumber.disabled());
        //实例数据
        nodeConfig.put(Constants.FIELD_PROPS, props);
        //组件配置
        result.put(Constants.FIELD_NODE_CONFIG, nodeConfig);
        //如果为真，代表预加载数据
        if (flag) {
            JSONArray nodeChild = new JSONArray();
            result.put(Constants.FIELD_NODE_CHILD, nodeChild);
        }
        return result;
    }
}
