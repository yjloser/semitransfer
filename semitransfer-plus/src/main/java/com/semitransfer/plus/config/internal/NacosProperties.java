package com.semitransfer.plus.config.internal;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * nacos配置信息
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-11-30 22:46
 * @version:2.0
 **/
@Data
@Component
@ConfigurationProperties(prefix = ConfigConstants.SEMITRANSFER_NACOS_PREFIX)
public class NacosProperties {

    /**
     * nacos服务地址
     */
    private String serverAddr;

    /**
     * 数据表示
     */
    private String dataId;

    /**
     * 分组
     */
    private String group="DEFAULT_GROUP";

    /**
     * 连接时间
     */
    private long time = 5000;

}
