package com.semitransfer.common.api.parse;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldOrder;
import com.baomidou.mybatisplus.annotation.page.Tree;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.util.StringUtils;

import java.lang.reflect.Field;

import static com.semitransfer.common.util.StringUtils.componentKey;

/**
 * <p>
 * Tree组件解析
 * </p>
 *
 * @author Mr.Yang
 * @since 2018-12-18
 */
public class TreeParse extends ParseBase {

    @Override
    public JSONObject parse(Field field, String options) {
        return parse(field, options, false);
    }

    @Override
    public JSONObject parse(Field field, String options, boolean flag) {
        //处理责任注解
        Tree treeField = field.getAnnotation(Tree.class);
        //返回结果
        JSONObject result = new JSONObject();
        //组件配置
        JSONObject nodeConfig = new JSONObject();
        //组件内置信息
        JSONObject props = new JSONObject();
        //没有数据时的提示
        props.put(Constants.FIELD_EMPTY_TEXT, treeField.showCheckbox());
        //是否显示多选框
        props.put(Constants.FIELD_SHOW_CHECKBOX, treeField.showCheckbox());
        //是否多选
        props.put(Constants.FIELD_MULTIPLE, treeField.multiple());
        //新增和更新时获取外部扩展
        if (options.startsWith(FieldOrder.SAVE.name()) || options.startsWith(FieldOrder.GET.name())) {
            //获取是否存在扩展
            String[] externalArrya = treeField.external();
            if (externalArrya.length > 0) {
                result.put(Constants.FIELD_EXTERNAL, externalArrya[0]);
            } else {
                result.put(Constants.FIELD_EXTERNAL, componentKey(field, Constants.COMPONENT_TREE));
            }
        }
        //可嵌套的节点属性的数组，生成 tree 的数据
        props.put(Constants.FIELD_DATA, treeField.data());
        //在显示复选框的情况下，是否严格的遵循父子不互相关联的做法
        props.put(Constants.FIELD_CHECK_STRICTLY, treeField.checkStrictly());
        nodeConfig.put(Constants.FIELD_PROPS, props);
        //组件配置
        result.put(Constants.FIELD_NODE_CONFIG, nodeConfig);
        //子节点配置
        if (!StringUtils.isEmptyEnhance(treeField.children())) {
            result.put(Constants.FIELD_NODE_CHILD, treeField.children());
        }
        //如果为真，代表预加载数据
        if (flag) {
            //获取外部组件
            String[] external = treeField.external();
            //目前支持一个外部组件
            if (external.length > 0) {
                return searchValue(external[0], result, treeField.refresh());
            }
            //获取查询信息
            return searchValue(componentKey(field, Constants.COMPONENT_TREE), result);
        }
        return result;
    }
}
