package com.semitransfer.plus.config.internal.annotation;

import java.lang.annotation.*;

/**
 * 登录拦截操作
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-11-03 13:02
 * @version:2.0
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginManage {

    /**
     * 拦截类型
     */
    LoginEnum value() default LoginEnum.EMNU;

    /**
     * 模块
     */
    String module() default "";

    /**
     * 分组
     */
    String group() default "";
}
