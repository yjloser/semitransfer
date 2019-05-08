package com.semitransfer.plus.config.internal.util.redis;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @program: zjsz-user
 * @description: Redis客户端类
 * @author: Mr.Yang
 * @create: 2018-06-30 21:58
 **/
@Component
@Data
@ConfigurationProperties(prefix = RedisProperties.REDIS_PREFIX)
public class RedisProperties extends JedisPoolConfig {

    public static final String REDIS_PREFIX = "spring.redis";

    private static Logger logger = LogManager.getLogger(RedisProperties.class);

    public static final int DEFAULT_DATABASE = 0;

    public RedisProperties() {
        super();
    }

    /**
     * 类加载时，自动初始化
     *
     * @author yjian
     * @date 9:33 2018/5/11
     * @version V1.0.0
     */
    @PostConstruct
    public void init() {
        if (!StringUtils.isEmpty(host)) {
            try {
                logger.info("------------- redis pool init start------------- ");
                JedisPoolConfig config = new JedisPoolConfig();
                // 设置池配置项值
                config.setMaxTotal(maxTotal == 0 ? 10 : maxTotal);
                config.setMaxWaitMillis(maxWaitMillis == 0 ? 30000 : maxWaitMillis);
                config.setMaxIdle(minIdle == 0 ? 5 : minIdle);
                config.setTestOnBorrow(testOnBorrow);
                //获取连接池
                pool = new JedisPool(config, host, port, timeout, password);
                boolean connected = isConnected();
                if (!connected) {
                    logger.error("redis 初始化出错 缓存服务器连接不上！ ");
                    throw new Exception("IP:" + host + ", redis服务器不可以连接~~~，请检查配置 与redis 服务器");
                }

                logger.info("------------- redis pool init end------------- ");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new Error("IP:" + host + ",设置redis服务器出错", e);
            }
        }
    }

    /**
     * 关闭服务时，自动销毁
     *
     * @author yjian
     * @date 9:32 2018/5/11
     * @version V1.0.0
     */
    @PreDestroy
    public void close() {
        pool.destroy();
    }

    /**
     * 验证是否连接正常
     *
     * @return boolean
     * @author yjian
     * @date 9:32 2018/5/11
     * @version V1.0.0
     */
    public boolean isConnected() {
        return getRedis().isConnected();
    }

    /**
     * 默认选择db0库
     *
     * @return Jedis
     * @author yjian
     * @date 9:37 2018/5/11
     * @version V1.0.0
     */
    public Jedis getRedis() {
        return getRedis(DEFAULT_DATABASE);
    }

    /**
     * 选择db库
     *
     * @param index db库标记
     * @return Jedis
     * @author yjian
     * @date 9:37 2018/5/11
     * @version V1.0.0
     */
    public Jedis getRedis(int index) {
        Jedis jedis = pool.getResource();
        jedis.select(index);
        return jedis;
    }

    /**
     * 返回连接池
     *
     * @param jedis 连接
     * @author yjian
     * @date 9:49 2018/5/11
     * @version V1.0.0
     */
    public void returnRedis(Jedis jedis) {
        jedis.close();
    }

    /**
     * 最大连接数
     **/
    private int maxTotal;
    /**
     * 最小连接数
     **/
    private int minIdle;
    /**
     * 最大等待时间
     **/
    private long maxWaitMillis;
    /**
     * 测试获取正确连接
     **/
    private boolean testOnBorrow;
    /**
     * 连接池
     **/
    private static JedisPool pool;
    /**
     * redis地址
     **/
    private String host;
    /**
     * redis端口
     **/
    private int port;
    /**
     * 超时时间
     **/
    private int timeout;
    /**
     * 鉴权
     **/
    private String password;
}