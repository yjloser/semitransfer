package com.semitransfer.plus.config.internal.annotation;

/**
 * 日志登记
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-11-03 13:02
 * @version:2.0
 **/
public enum LoggerEnum {

    /**
     * 日志登记
     */
    ERROR("ERROR"),
    WARN("WARN"),
    INFO("INFO"),
    DEBUG("DEBUG");

    private String value;

    LoggerEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
