package com.semitransfer.plus.config.internal.util.redis;

import redis.clients.jedis.Jedis;

/**
 * @program: zjsz-user
 * @description: Redis回调模板
 * @author: Mr.Yang
 * @create: 2018-06-30 21:58
 **/
public interface RedisCallback<T> {

    /**
     * 回调
     *
     * @param jedis  连接源
     * @param params 参数
     * @return T 泛型
     * @author yjian
     * @date 9:55 2018/5/11
     * @version V1.0.0
     */
    public T call(Jedis jedis, Object params);
}
