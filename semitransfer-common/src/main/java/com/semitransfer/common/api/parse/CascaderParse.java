package com.semitransfer.common.api.parse;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldOrder;
import com.baomidou.mybatisplus.annotation.page.Cascader;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.util.StringUtils;

import java.lang.reflect.Field;

import static com.semitransfer.common.util.StringUtils.componentKey;

/**
 * <p>
 * 解析Cascader组件
 * </p>
 *
 * @author Mr.Yang
 * @since 2018-12-19
 */
public class CascaderParse extends ParseBase {

    @Override
    public JSONObject parse(Field field, String options) {
        return parse(field, options, false);
    }

    @Override
    public JSONObject parse(Field field, String options, boolean flag) {
        //处理责任注解
        Cascader cascaderField = field.getAnnotation(Cascader.class);
        //返回结果
        JSONObject result = new JSONObject();
        //组件配置
        JSONObject nodeConfig = new JSONObject();
        //组件内置信息
        JSONObject props = new JSONObject();
        //是否可清空
        props.put(Constants.FIELD_CLEARABLE, cascaderField.clearable());
        //占位文本
        props.put(Constants.FIELD_PLACEHOLDER, StringUtils.notEmptyEnhance(cascaderField.placeholder()) ?
                cascaderField.placeholder() : null);
        ///是否禁用
        props.put(Constants.FIELD_DISABLED, cascaderField.disabled());
        //新增和更新时获取外部扩展
        if (options.startsWith(FieldOrder.SAVE.name()) || options.startsWith(FieldOrder.GET.name())) {
            //获取是否存在扩展
            String[] externalArrya = cascaderField.external();
            if (externalArrya.length > 0) {
                result.put(Constants.FIELD_EXTERNAL, externalArrya[0]);
            } else {
                result.put(Constants.FIELD_EXTERNAL, componentKey(field, Constants.COMPONENT_CASCADER));
            }
        }
        //是否支持搜索
        props.put(Constants.FIELD_FILTERABLE, cascaderField.filterable());
        //实例数据
        nodeConfig.put(Constants.FIELD_PROPS, props);
        //组件配置
        result.put(Constants.FIELD_NODE_CONFIG, nodeConfig);
        //正则信息
        result.put(Constants.FIELD_RULES, cascaderField.rules());
        //如果为真，代表预加载数据
        if (flag) {
            //获取外部组件
            String[] external = cascaderField.external();
            //目前支持一个外部组件
            if (external.length > 0) {
                return searchValue(external[0], result, cascaderField.refresh());
            }
            return searchValue(componentKey(field, Constants.COMPONENT_CASCADER), result);
        }
        return result;
    }
}
