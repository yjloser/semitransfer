package com.semitransfer.plus.config.internal.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 权限操作
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-11-03 13:02
 * @version:2.0
 **/
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AclManage {

    /**
     * 权限名
     */
    String value() default "";
}
