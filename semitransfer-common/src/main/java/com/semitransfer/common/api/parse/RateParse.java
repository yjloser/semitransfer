package com.semitransfer.common.api.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.page.Rate;
import com.semitransfer.common.api.Constants;

import java.lang.reflect.Field;

/**
 * <p>
 * Rate 组件解析
 * </p>
 *
 * @author Mr.Yang
 * @since 2018-12-18
 */
public class RateParse extends ParseBase {

    @Override
    public JSONObject parse(Field field, String options) {
        return parse(field, options, false);
    }

    @Override
    public JSONObject parse(Field field, String options, boolean flag) {
        //处理责任注解
        Rate rateField = field.getAnnotation(Rate.class);
        //返回结果
        JSONObject result = new JSONObject();
        //组件配置
        JSONObject nodeConfig = new JSONObject();
        //组件内置信息
        JSONObject props = new JSONObject();
        //star 总数
        props.put(Constants.FIELD_COUNT, rateField.count());
        //当前 star 数，可以使用 v-model 双向绑定数据
        props.put(Constants.FIELD_VALUE, rateField.value());
        ///是否禁用
        props.put(Constants.FIELD_DISABLED, rateField.disabled());
        //是否允许半选
        props.put(Constants.FIELD_ALLOW_HALF, rateField.allowHalf());
        //是否显示提示文字
        props.put(Constants.FIELD_SHOW_TEXT, rateField.showText());
        //是否可清空
        props.put(Constants.FIELD_CLEARABLE, rateField.clearable());
        //自定义字符
        props.put(Constants.FIELD_CHARACTER, rateField.character());
        //使用图标
        props.put(Constants.FIELD_ICON, rateField.icon());
        //使用自定义图标
        props.put(Constants.FIELD_CUSTOM_ICON, rateField.customIcon());
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
