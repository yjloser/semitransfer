package com.semitransfer.common.api.response;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldOrder;
import com.semitransfer.common.api.AbstractResponse;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.api.parse.AnnotationParse;

import javax.servlet.http.HttpServletResponse;

import static com.semitransfer.common.encrypt.AnalyzeUtils.getCodeValue;

/**
 * 桌面端响应
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @create: 2018-12-02 16:08
 * @version:2.0
 **/
public class DesktopEndResponse extends AbstractResponse {

    private static final long serialVersionUID = 3700838367361208237L;


    /**
     * 新增失败
     *
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject responseMessageSaveFail() {
        return responseMessage(40011, "新增失败");
    }

    /**
     * 更新失败
     *
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject responseMessageUpdateFail() {
        return responseMessage(40012, "更新失败");
    }

    /**
     * 删除失败
     *
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject responseMessageRemoveFail() {
        return responseMessage(40013, "删除失败");
    }


    /**
     * 组装信息返回
     *
     * @param code 返回码
     * @param msg  返回信息
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    private static JSONObject responseMessage(Integer code, String msg) {
        JSONObject result = new JSONObject();
        //结果信息
        result.put(Constants.FIELD_CODE, code);
        //获取返回码对应的信息
        result.put(Constants.FIELD_MSG, msg);
        return result;
    }


    /**
     * 默认返回成功
     *
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject responseMessage() {
        return responseMessage(Constants.NUM_ZERO);
    }


    /**
     * 默认返回成功
     *
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject responseMessage(Class<?> clazz) {
        return responseMessage(Constants.NUM_ZERO, clazz, FieldOrder.LIST);
    }

    /**
     * 默认编辑返回成功
     *
     * @param clazz 实体
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject responseGetMessage(Class<?> clazz) {
        return responseMessage(Constants.NUM_ZERO, clazz, FieldOrder.GET);
    }

    /**
     * 默认新增返回成功
     *
     * @param response 响应对象
     * @param clazz    实体字段
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseSaveMessage(HttpServletResponse response, Class<?> clazz) {
        responseMessage(Constants.NUM_ZERO, response, clazz, FieldOrder.SAVE);
    }


    /**
     * 默认检索返回成功
     *
     * @param clazz 实体
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject responseSearchMessage(Class<?> clazz) {
        return responseMessage(Constants.NUM_ZERO, clazz, FieldOrder.SEARCH);
    }


    /**
     * 默认返回成功及列表
     *
     * @param clazz 反射实体类
     * @param order 排序模式
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject responseMessage(Class<?> clazz, FieldOrder order) {
        return responseMessage(Constants.NUM_ZERO, clazz, order);
    }

    /**
     * 默认返回成功
     *
     * @param code  返回码
     * @param clazz 反射实体类
     * @param order 排序模式
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject responseMessage(Integer code, Class<?> clazz, FieldOrder order) {
        //获取返回值
        JSONObject outcome = responseMessage(code);
        //反射实体类用于前端展示
        outcome.put(Constants.FIELD_COLUMN, new AnnotationParse().reflectEntityClass(clazz, order));
        return outcome;
    }


    /**
     * 默认返回成功
     *
     * @param column 手动前端展示列
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject responseMessage(String column) {
        //获取返回值
        JSONObject outcome = responseMessage(Constants.NUM_ZERO);
        //反射实体类用于前端展示
        outcome.put(Constants.FIELD_COLUMN, column);
        return outcome;
    }

    /**
     * 返回平台全局码
     *
     * @param code 返回码
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject responseMessage(Integer code) {
        return responseMessage(code, false);
    }


    /**
     * 返回平台全局码
     *
     * @param code   返回码
     * @param change 是否使用默认成功返回码0
     * @return JSONObject json类型
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static JSONObject responseMessage(Integer code, boolean change) {
        JSONObject result = new JSONObject();
        result.put(Constants.FIELD_CODE, code);
        // 如果为true，则需要替换一下返回码
        if (change) {
            result.put(Constants.FIELD_CODE, Constants.NUM_ZERO);
        }
        //获取返回码对应的信息
        result.put(Constants.FIELD_MSG, getCodeValue(String.valueOf(code)));
        return result;
    }

    /**
     * 默认成功
     *
     * @param response 响应对象
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(HttpServletResponse response) {
        responseMessage(Constants.NUM_ZERO, response);
    }

    /**
     * 直接返回平台信息，适用于增删改操作
     *
     * @param code     返回码
     * @param response 响应对象
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(Integer code, HttpServletResponse response) {
        //获取信息
        JSONObject outcome = new JSONObject();
        outcome.put(Constants.FIELD_CODE, code);
        //获取返回码对应的信息
        outcome.put(Constants.FIELD_MSG, getCodeValue(String.valueOf(code)));
        //直接响应
        write(outcome, response);
    }

    /**
     * 默认成功返回平台信息，适用于增删改操作
     *
     * @param response 响应对象
     * @param clazz    反射自动生成列
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(HttpServletResponse response, Class<?> clazz) {
        responseMessage(Constants.NUM_ZERO, response, clazz, FieldOrder.LIST);
    }

    /**
     * 默认成功返回平台信息，适用于增删改操作
     *
     * @param response 响应对象
     * @param clazz    反射自动生成列
     * @param order    排序模式
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(HttpServletResponse response, Class<?> clazz, FieldOrder order) {
        responseMessage(Constants.NUM_ZERO, response, clazz, order);
    }

    /**
     * 直接返回平台信息，适用于增删改操作
     *
     * @param code     返回码
     * @param response 响应对象
     * @param clazz    反射自动生成列
     * @param order    排序模式
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(Integer code, HttpServletResponse response, Class<?> clazz, FieldOrder order) {
        responseMessage(responseMessage(code), response, clazz, order);
    }

    /**
     * 返回响应信息，适用于复杂返回信息
     *
     * @param column   前端展示字段
     * @param outcome  响应信息
     * @param response 响应对象
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(JSONObject outcome, JSONObject column, HttpServletResponse response) {
        //直接响应
        responseMessage(outcome, column.toJSONString(), response);
    }

    /**
     * 返回响应信息，适用于复杂返回信息
     *
     * @param column   前端展示字段
     * @param outcome  响应信息
     * @param response 响应对象
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(JSONObject outcome, String column, HttpServletResponse response) {
        outcome.put(Constants.FIELD_COLUMN, column);
        //直接响应
        responseMessage(outcome, response);
    }


    /**
     * 返回响应信息，适用于复杂返回信息
     *
     * @param outcome  响应信息
     * @param response 响应对象
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(JSONObject outcome, HttpServletResponse response) {
        //直接响应
        responseMessage(outcome, response, null, null);
    }

    /**
     * 默认成功
     *
     * @param outcome  响应信息
     * @param response 响应对象
     * @param clazz    反射自动生成列
     * @param order    排序模式
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static void responseMessage(JSONObject outcome, HttpServletResponse response, Class<?> clazz, FieldOrder order) {
        if (clazz != null) {
            //反射实体类用于前端展示
            outcome.put(Constants.FIELD_COLUMN, new AnnotationParse().reflectEntityClass(clazz, order));
        }
        //响应前台
        write(outcome, response);
    }
}
