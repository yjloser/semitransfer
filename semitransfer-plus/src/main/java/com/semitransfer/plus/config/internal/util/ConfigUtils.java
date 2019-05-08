package com.semitransfer.plus.config.internal.util;

import com.semitransfer.plus.config.internal.ConfigConstants;

import java.util.Objects;

/**
 * 配置工具
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-12-02 13:30
 * @version:2.0
 **/
public class ConfigUtils {


    /**
     * 获取文件路径
     *
     * @return 返回文件路径
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static String getFilePath() {
        //获取项目路径
        String projectPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
        //判断操作系统
        if (System.getProperty(ConfigConstants.OS_NAME).toLowerCase().contains(ConfigConstants.WINDOWS)) {
            //处理前置路径
            String preposePath = projectPath.substring(1);
            //处理
            projectPath = preposePath.replaceAll("/", "\\\\");
        } else {
            //如果存在file开头，截取掉
            if (projectPath.startsWith(ConfigConstants.FIELD_FILE)) {
                projectPath = projectPath.substring(5);
            }
        }
        return projectPath;
    }
}
