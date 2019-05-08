package com.semitransfer.plus.config.internal.annotation;

import org.springframework.core.annotation.AliasFor;

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
public @interface LoggerManage {

    /**
     * 模块名称
     */
    @AliasFor("module")
    String value() default "";

    /**
     * 模块
     */
    @AliasFor("value")
    String module() default "";

    /**
     * 日志登记
     */
    LoggerEnum level() default LoggerEnum.DEBUG;

}
