package com.semitransfer.common.encrypt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.semitransfer.common.api.Constants;
import com.semitransfer.common.util.FileUtils;
import com.semitransfer.common.util.StringUtils;
import com.semitransfer.plus.config.internal.ConfigConstants;
import com.semitransfer.plus.config.internal.util.redis.RedisTempalte;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static com.semitransfer.plus.config.internal.util.ConfigUtils.getFilePath;

/**
 * 解析请求工具类
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-07-07 10:31
 * @version:2.0
 **/
@Component
@Data
@ConfigurationProperties(prefix = Constants.FIELD_ANALYZES)
public class AnalyzeUtils {

    private static Logger logger = LoggerFactory.getLogger(AnalyzeUtils.class);

    /**
     * 字符串模板
     */
    @Autowired
    private RedisTempalte redisTempalte;

    /**
     * 密钥
     */
    private String key;

    /**
     * 外部使用
     */
    public static String STATIC_KEYS;

    /**
     * 邮件信息
     */
    public static String emailContent;

    /**
     * 返回码缓存在内存中
     */
    public static Map<String, String> codeMap = new ConcurrentHashMap<>();

    /**
     * 检索信息
     */
    public static Map<Object, Object> searchMap = new ConcurrentHashMap<>();

    /**
     * 默认加载的properties文件
     */
    private static final String[] DEFAULT_PROPERTIES_URL = {"config/responseMessage.properties", "config/email.txt"};

    static {
        loadFile(DEFAULT_PROPERTIES_URL[0]);
        loadHtml(DEFAULT_PROPERTIES_URL[1]);
    }

    /**
     * 返回响应信息
     *
     * @param code 响应码
     * @return 返回响应信息
     * @author Mr.Yang
     * @date 2019/2/27 0027
     */
    public static String getCodeValue(String code) {
        if (codeMap.size() == 0) {
            loadFile(DEFAULT_PROPERTIES_URL[0]);
            loadHtml(DEFAULT_PROPERTIES_URL[1]);
        }
        return StringUtils.isEmpty(codeMap.get(code)) ? "" : codeMap.get(code);
    }


    /**
     * 初始化获取返回码
     *
     * @author Mr.Yang
     * @date 2018/7/4 0004
     */
    @PostConstruct
    public void init() {
        try {
            // 给静态赋值
            STATIC_KEYS = key;
        } catch (Exception e) {
            logger.error("初始化密钥失败", e);
        }
    }

    /**
     * 定时执行返回码
     *
     * @author Mr.Yang
     * @date 2018/12/2
     */
    @Scheduled(fixedRate = 1000000)
    public void scheduledCode() {
        logger.debug("=====重新载入全局响应码=====");
        //文件是否存在
        if (new File(getFilePath() + DEFAULT_PROPERTIES_URL[0]).exists()) {
            //加载文件
            loadFile(DEFAULT_PROPERTIES_URL[0]);
        }
        //文件是否存在
        if (new File(getFilePath() + DEFAULT_PROPERTIES_URL[1]).exists()) {
            //加载文件
            loadHtml(DEFAULT_PROPERTIES_URL[1]);
        }
    }

    /**
     * 刷新缓存信息
     *
     * @param key key
     * @param key value
     * @return 返回true表示成功
     * @author Mr.Yang
     * @date 2019/3/12 0012
     */
    public boolean refresh(String key, String value) {
        try {
            //覆盖新值
            long result = this.redisTempalte.hset(ConfigConstants.NUM_ZERO, Constants.SEARCH_DATA, key, value);
            //查询全部信息
            JSONObject allData = new JSONObject();
            //查询所有的信息
            this.redisTempalte.hgetAll(ConfigConstants.NUM_ZERO, Constants.SEARCH_DATA)
                    .forEach((k, v) -> allData.put(String.valueOf(k), JSON.parseArray(v)));
            allData.forEach((k, v) -> AnalyzeUtils.searchMap.put(k, v));
            if (result > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 加载配置文件信息
     *
     * @param fileUrl 文件路径
     * @author Mr.Yang
     * @date 2018/12/2
     */
    private static void loadFile(String fileUrl) {
        //文件全路径
        if (new File(getFilePath() + fileUrl).exists()) {
            try {
                //从指定路径加载信息
                Properties properties =
                        PropertiesLoaderUtils.loadProperties(new EncodedResource(new ClassPathResource(fileUrl), StandardCharsets.UTF_8));
                //清空
                codeMap.clear();
                //将返返回码放入map中
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    codeMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
            } catch (Exception e) {
                logger.error("Exception when load" + fileUrl, e);
            }
        }
    }

    /**
     * 加载邮箱内容信息
     *
     * @param fileUrl 文件路径
     * @author Mr.Yang
     * @date 2018/12/2
     */
    private static void loadHtml(String fileUrl) {
        //文件全路径
        if (new File(getFilePath() + fileUrl).exists()) {
            try {
                emailContent = String.valueOf(FileUtils.readAsStringBuilder(getFilePath() + fileUrl));
            } catch (Exception e) {
                logger.error("Exception when load" + fileUrl, e);
            }
        }
    }
}
