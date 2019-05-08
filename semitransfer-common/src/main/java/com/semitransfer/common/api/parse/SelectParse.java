package com.semitransfer.common.api.parse;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldOrder;
import com.baomidou.mybatisplus.annotation.page.Select;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.util.StringUtils;

import java.lang.reflect.Field;

import static com.semitransfer.common.util.StringUtils.componentKey;

/**
 * <p>
 * select组件解析
 * </p>
 *
 * @author Mr.Yang
 * @since 2018-12-18
 */
public class SelectParse extends ParseBase {

    @Override
    public JSONObject parse(Field field, String options) {
        return parse(field, options, false);
    }

    @Override
    public JSONObject parse(Field field, String options, boolean flag) {
        //处理责任注解
        Select selectField = field.getAnnotation(Select.class);
        //返回结果
        JSONObject result = new JSONObject();
        //组件配置
        JSONObject nodeConfig = new JSONObject();
        //组件内置信息
        JSONObject props = new JSONObject();
        //提示
        props.put(Constants.FIELD_PLACEHOLDER, StringUtils.notEmptyEnhance(selectField.placeholder()) ?
                selectField.placeholder() : null);
        //是否可清空
        props.put(Constants.FIELD_CLEARABLE, selectField.clearable());
        //新增和更新时获取外部扩展
        if (options.startsWith(FieldOrder.SAVE.name()) || options.startsWith(FieldOrder.GET.name())) {
            //获取是否存在扩展
            String[] externalArrya = selectField.external();
            if (externalArrya.length > 0) {
                result.put(Constants.FIELD_EXTERNAL, externalArrya[0]);
            } else {
                result.put(Constants.FIELD_EXTERNAL, componentKey(field, Constants.COMPONENT_SELECT));
            }
        }
        ///是否禁用
        props.put(Constants.FIELD_DISABLED, selectField.disabled());
        //是否多选
        props.put(Constants.FIELD_MULTIPLE, selectField.multiple());
        //是否支持搜索
        props.put(Constants.FIELD_FILTERABLE, selectField.filterable());
        //实例数据
        nodeConfig.put(Constants.FIELD_PROPS, props);
        //组件配置
        result.put(Constants.FIELD_NODE_CONFIG, nodeConfig);
        //正则
        result.put(Constants.FIELD_RULES, selectField.rules());
        //如果为真，代表预加载数据
        if (flag) {
            //获取外部组件
            String[] external = selectField.external();
            //目前支持一个外部组件
            if (external.length > 0) {
                return searchValue(external[0], result, selectField.refresh());
            }
            //获取外部应用信息
            searchValue(componentKey(field, Constants.COMPONENT_SELECT), result);
            if (StringUtils.notEmpty(result.getString(Constants.FIELD_DATA))) {
                return result;
            } else if (StringUtils.notEmpty(selectField.values())) {
                result.put(Constants.FIELD_DATA, selectField.values());
                return result;
            }
        }
        return result;
    }
}
