package com.semitransfer.common.api.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldOrder;
import com.baomidou.mybatisplus.annotation.PageField;
import com.baomidou.mybatisplus.annotation.page.Select;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 自定义注解处理模板
 * </p>
 *
 * @author Mr.Yang
 * @since 2018-12-18
 */
public class AnnotationParse {

    /**
     * 在其他组件之前处理
     *
     * @author Mr.Yang
     * @date 2018/12/18 0018
     */
    public JSONArray reflectEntityClass(Class<?> clazz, FieldOrder fieldOrder) {
        if (clazz != null) {
            Map<Integer, JSONObject> unsortMap = new HashMap<>(Constants.NUM_TEN);
            //处理实体类所有字段
            Field[] fields = clazz.getDeclaredFields();
            //列表信息
            JSONArray column = new JSONArray();
            //临时信息
            JSONArray tempColumn = new JSONArray();
            AtomicInteger atomicCount = new AtomicInteger(Constants.NUM_ZERO);
            //处理标记有PageField注解的属性
            Arrays.stream(fields).filter
                    (field -> field.isAnnotationPresent(PageField.class)).forEach(field -> {
                //获取注解内容
                PageField pageField = field.getAnnotation(PageField.class);
                JSONObject td = new JSONObject();
                //获取是否设置显示列表
                FieldOrder[] shows = pageField.show();
                //添加返回字段
                td.put(Constants.FIELD_KEY, field.getName());
                //show标签处理
                if (shows.length > Constants.NUM_ZERO) {
                    Optional<FieldOrder> checkAnnotation;
                    //处理注解问题
                    switch (fieldOrder.name()) {
                        //列表查询
                        case Constants.FIELD_LIST_UPCASE:
                            //处理列表注解
                            checkAnnotation = Arrays.stream(shows).filter(show ->
                                    show.name().startsWith(Constants.FIELD_ALL) ||
                                            show.name().startsWith(Constants.FIELD_COM) ||
                                            show.name().startsWith(Constants.FIELD_LIST_UPCASE)
                            ).findFirst();
                            if (checkAnnotation.isPresent()) {
                                //处理过程
                                forEachProcess(atomicCount, td, unsortMap,
                                        field, pageField, fieldOrder, processList(shows), tempColumn);
                            }
                            break;
                        //编辑查询
                        case Constants.FIELD_GET_UPCASE:
                            //处理编辑注解
                            checkAnnotation = Arrays.stream(shows).filter(show ->
                                    show.name().startsWith(Constants.FIELD_ALL) ||
                                            show.name().startsWith(Constants.FIELD_COM) ||
                                            show.name().startsWith(Constants.FIELD_GET_UPCASE)
                            ).findFirst();
                            if (checkAnnotation.isPresent()) {
                                //处理过程
                                forEachProcess(atomicCount, td, unsortMap,
                                        field, pageField, fieldOrder, processGet(shows), tempColumn);
                            }
                            break;
                        //新增
                        case Constants.FIELD_SAVE_UPCASE:
                            //处理新增注解
                            checkAnnotation = Arrays.stream(shows).filter(show ->
                                    show.name().startsWith(Constants.FIELD_ALL) ||
                                            show.name().startsWith(Constants.FIELD_COM) ||
                                            show.name().startsWith(Constants.FIELD_SAVE_UPCASE)
                            ).findFirst();
                            if (checkAnnotation.isPresent()) {
                                //处理过程
                                forEachProcess(atomicCount, td, unsortMap,
                                        field, pageField, fieldOrder, processSave(shows), tempColumn);
                            }
                            break;
                        //检索
                        case Constants.FIELD_SEARCH_UPCASE:
                            //处理检索注解
                            checkAnnotation = Arrays.stream(shows).filter(show ->
                                    show.name().startsWith(Constants.FIELD_SEARCH_UPCASE) ||
                                            show.name().startsWith(Constants.FIELD_ALL)
                            ).findFirst();
                            if (checkAnnotation.isPresent()) {
                                //处理过程
                                forEachProcess(atomicCount, td, unsortMap,
                                        field, pageField, fieldOrder, processSearch(shows), tempColumn);
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    tempColumn.add(td);
                    unsortMap.put(1000 + atomicCount.getAndIncrement(), td);
                }
            });
            if (atomicCount.get() != unsortMap.size()) {
                return tempColumn;
            }
            //处理排序
            unsortMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(x -> column.add(x.getValue()));
            //加入展示列表
            return column;
        }
        return null;
    }

    /**
     * 处理其他注解
     *
     * @param shows 注解数组
     * @return 返回排序
     * @author Mr.Yang
     * @date 2019/2/14 0014
     */
    private int processOthers(FieldOrder[] shows) {
        //取出com注解
        Optional<FieldOrder> tempAnnotation =
                Arrays.stream(shows).filter(show -> show.name().startsWith(Constants.FIELD_COM)).findFirst();
        if (tempAnnotation.isPresent()) {
            return tempAnnotation.get().getOrder();
        }
        //取出all注解
        tempAnnotation =
                Arrays.stream(shows).filter(show -> show.name().startsWith(Constants.FIELD_ALL)).findFirst();
        return tempAnnotation.map(FieldOrder::getOrder).orElse(9000);
    }

    /**
     * 处理检索注解
     *
     * @param shows 注解数组
     * @return 返回排序
     * @author Mr.Yang
     * @date 2019/2/14 0014
     */
    private int processSearch(FieldOrder[] shows) {
        //取出search注解
        Optional<FieldOrder> tempAnnotation =
                Arrays.stream(shows).filter(show -> show.name().startsWith(Constants.FIELD_SEARCH_UPCASE)).findFirst();
        if (tempAnnotation.isPresent()) {
            return tempAnnotation.get().getOrder();
        }
        //取出all注解
        tempAnnotation =
                Arrays.stream(shows).filter(show -> show.name().startsWith(Constants.FIELD_ALL)).findFirst();
        return tempAnnotation.map(FieldOrder::getOrder).orElse(9000);
    }

    /**
     * 处理新增注解
     *
     * @param shows 注解数组
     * @return 返回排序
     * @author Mr.Yang
     * @date 2019/2/14 0014
     */
    private int processSave(FieldOrder[] shows) {
        //取出最高优先级save注解
        Optional<FieldOrder> tempAnnotation =
                Arrays.stream(shows).filter(show -> show.name().startsWith(Constants.FIELD_SAVE_UPCASE)).findFirst();
        return tempAnnotation.map(FieldOrder::getOrder).orElseGet(() -> processOthers(shows));
    }

    /**
     * 处理编辑注解
     *
     * @param shows 注解数组
     * @return 返回排序
     * @author Mr.Yang
     * @date 2019/2/14 0014
     */
    private int processGet(FieldOrder[] shows) {
        //取出最高优先级get注解
        Optional<FieldOrder> tempAnnotation =
                Arrays.stream(shows).filter(show -> show.name().startsWith(Constants.FIELD_GET_UPCASE)).findFirst();
        return tempAnnotation.map(FieldOrder::getOrder).orElseGet(() -> processOthers(shows));
    }

    /**
     * 处理列表注解
     *
     * @param shows 注解数组
     * @return 返回排序
     * @author Mr.Yang
     * @date 2019/2/14 0014
     */
    private int processList(FieldOrder[] shows) {
        //取出最高优先级list注解
        Optional<FieldOrder> tempAnnotation =
                Arrays.stream(shows).filter(show -> show.name().startsWith(Constants.FIELD_LIST_UPCASE)).findFirst();
        return tempAnnotation.map(FieldOrder::getOrder).orElseGet(() -> processOthers(shows));
    }

    /**
     * 注解综合处理
     *
     * @param atomicCount 统计
     * @param td          字段属性
     * @param unsortMap   排序
     * @param field       字段属性对象
     * @param pageField   注解
     * @param fieldOrder  功能名称
     * @param order       顺序
     * @param tempColumn  临时列
     * @author Mr.Yang
     * @date 2019/2/14 0014
     */
    private void forEachProcess(AtomicInteger atomicCount, JSONObject td,
                                Map<Integer, JSONObject> unsortMap, Field field,
                                PageField pageField, FieldOrder fieldOrder, int order, JSONArray tempColumn) {
        //自增计数
        atomicCount.getAndIncrement();
        //获取字段名称
        td.put(Constants.FIELD_LABLE, pageField.label());
        //储存顺序
        unsortMap.put(order, td);
        //其他组件分支处理
        Annotation[] annotations = field.getAnnotations();
        //获取单独标记组件
        Optional<Annotation> annotationResult =
                Arrays.stream(annotations).filter(annotation ->
                        !annotation.toString().contains(Constants.TABLE_FIELD)
                                && !annotation.toString().contains(Constants.PAGE_FIELD)).findFirst();
        //单独标记select
        if (field.isAnnotationPresent(Select.class) && fieldOrder.name().startsWith(FieldOrder.LIST.name())) {
            Select selectField = field.getAnnotation(Select.class);
            td.put(Constants.FIELD_VALUE, StringUtils.notEmptyEnhance(selectField.values())
                    ? selectField.values() : null);
        }
        if (annotationResult.isPresent() &&
                !fieldOrder.name().startsWith(Constants.FIELD_LIST.toUpperCase())) {
            //截取解析该组件的类
            String component = annotationResult.get().annotationType().toString()
                    .substring(annotationResult.get().annotationType().toString()
                            .lastIndexOf(".") + Constants.NUM_ONE);
            //组件类型
            td.put(Constants.FIELD_NODE, component);
            //反射处理组件问题
            component = "com.semitransfer.common.api.parse.".concat(component).concat("Parse");
            try {
                Class<?> componentClass = Class.forName(component);
                //初始化
                ParseBase base = (ParseBase) componentClass.getConstructor().newInstance();
                //执行具体的解析
                JSONObject parseResult = Constants.FIELD_SEARCH.toUpperCase().equals(fieldOrder.name())
                        ? base.parse(field, fieldOrder.name(), true) : base.parse(field, fieldOrder.name());
                td.putAll(parseResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tempColumn.add(td);
    }
}
