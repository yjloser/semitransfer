package com.semitransfer.common.api.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.page.DatePicker;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.util.StringUtils;

import java.lang.reflect.Field;

/**
 * <p>
 * 解析DatePickerParse组件
 * </p>
 *
 * @author Mr.Yang
 * @since 2018-12-19
 */
public class DatePickerParse extends ParseBase {

    @Override
    public JSONObject parse(Field field, String options) {
        return parse(field, options, false);
    }

    @Override
    public JSONObject parse(Field field, String options, boolean flag) {
        //处理责任注解
        DatePicker datePicker = field.getAnnotation(DatePicker.class);
        //返回结果
        JSONObject result = new JSONObject();
        //组件配置
        JSONObject nodeConfig = new JSONObject();
        //组件内置信息
        JSONObject props = new JSONObject();
        //占位文本
        props.put(Constants.FIELD_PLACEHOLDER, StringUtils.notEmptyEnhance(datePicker.placeholder()) ?
                datePicker.placeholder() : null);
        //显示类型
        props.put(Constants.FIELD_TYPE, StringUtils.notEmptyEnhance(datePicker.type()) ?
                datePicker.placeholder() : "date");
        //展示的日期格式
        props.put(Constants.FIELD_FORMAT, StringUtils.notEmptyEnhance(datePicker.format()) ?
                datePicker.format() : "yyyy-MM-dd");
        //日期选择器出现的位置
        props.put(Constants.FIELD_PLACEMENT, StringUtils.notEmptyEnhance(datePicker.placement())
                ? datePicker.placement() : "datePicker.placement");
        //开启后，可以选择多个日期
        props.put(Constants.FIELD_MULTIPLE, datePicker.multiple());
        //输入框尺寸
        props.put(Constants.FIELD_SIZE, StringUtils.notEmpty(datePicker.size()) ? datePicker.size() : null);
        //设置输入框为只读
        props.put(Constants.FIELD_READONLY, datePicker.readonly());
        //是否可编辑
        props.put(Constants.FIELD_EDITABLE, datePicker.editable());
        //是否禁用
        props.put(Constants.FIELD_DISABLED, datePicker.disabled());
        //是否显示清除按钮
        props.put(Constants.FIELD_CLEARABLE, datePicker.clearable());
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
