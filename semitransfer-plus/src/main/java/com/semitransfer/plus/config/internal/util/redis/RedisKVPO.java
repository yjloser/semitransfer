package com.semitransfer.plus.config.internal.util.redis;

import lombok.Data;

/**
 * @program: zjsz-user
 * @description: Key-Value实体类
 * @author: Mr.Yang
 * @create: 2018-06-30 21:58
 **/
@Data
public class RedisKVPO {
    /**
     * key
     **/
    private String k;

    /**
     * 值
     **/
    private String v;

    public RedisKVPO() {
    }

    public RedisKVPO(String k, String v) {
        this.k = k;
        this.v = v;
    }
}