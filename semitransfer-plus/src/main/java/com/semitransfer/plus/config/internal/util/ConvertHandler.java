package com.semitransfer.plus.config.internal.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.semitransfer.plus.config.internal.ConfigConstants;
import com.semitransfer.plus.config.internal.ConfigEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.semitransfer.plus.config.internal.util.ConfigUtils.getFilePath;

/**
 * 数据库表转实体处理器
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-11-29 15:01
 * @version:2.0
 **/
public class ConvertHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConvertHandler.class);


    /**
     * 缓存code信息
     *
     * @param codes code信息
     * @return 返回结果
     * @author Mr.Yang
     * @date 2018/11/29 0029
     */
    public static boolean cacheCode(String codes) {
        // 转换JSONArray
        JSONArray codeArray;
        try {
            codeArray = JSONArray.parseArray(codes);
        } catch (Exception e) {
            logger.error("请求参数转换JSONArray格式错误", e);
            return false;
        }
        //处理properties
        return appendMsgToProperties(codeArray);
    }

    /**
     * 拼接信息到properties
     *
     * @param codeArray 字符数组code
     * @return 如果成功返回true
     * @author Mr.Yang
     * @date 2018/12/2
     */
    public static boolean appendMsgToProperties(JSONArray codeArray) {
        //从指定路径加载信息
        try {
            //获取文件夹路径
            File filePath = new File(getFilePath() + "config/");
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            //判断文件是否存在
            File file = new File(getFilePath() + ConfigConstants.CODE_PATH);
            if (!file.exists()) {
                file.createNewFile();
            }
            //返回码列表
            List<String> codeList = new ArrayList<>();
            //循环插入
            codeArray.forEach(code -> {
                //转换为json
                JSONObject tempCode = JSON.parseObject(String.valueOf(code));
                //处理写入内容
                codeList.add(tempCode.getString(ConfigConstants.FIELD_CODE).concat("=")
                        .concat(tempCode.getString(ConfigConstants.FIELD_MSG)));
            });
            //写入文件中
            writeLines(file, codeList);
            //获取文件夹路径
            String filePathStr = getFilePath().substring(0, getFilePath().indexOf("target")).concat("src/main/");
            //判断如果存在resources则表示本地
            if (new File(filePathStr + "resources").exists()) {
                filePath = new File(filePathStr + "resources/config");
                if (!filePath.exists()) {
                    filePath.mkdirs();
                }
                //判断文件是否存在
                file = new File(filePathStr + "resources/" + ConfigConstants.CODE_PATH);
                if (!file.exists()) {
                    file.createNewFile();
                }
                //写入文件中
                writeLines(file, codeList);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 处理转换方法
     *
     * @param convert 转换配置信息
     * @return 返回结果
     * @author Mr.Yang
     * @date 2018/11/29 0029
     */
    public static boolean convertTableToEntity(String convert) {
        //转换json
        JSONObject params = JSONObject.parseObject(convert);
        //校验必填字段
        if (!checkParam(params, ConfigConstants.JDBC_URL,
                ConfigConstants.JDBC_USER_NAME, ConfigConstants.JDBC_USER_PASSWORD, ConfigConstants.FIELD_PREFIX,
                ConfigConstants.FIELD_BASE_PACKAGE)) {
            return false;
        }
        //生成文件
        AutoGenerator generator = new AutoGenerator();
        //全局配置
        globalConfig(generator, params);
        //注入自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
            }
        };
        generator.setCfg(cfg);
        //自定义模板
        TemplateConfig tc = new TemplateConfig();
        //实体层模板
        tc.setController(params.containsKey(ConfigConstants.FIELD_ENTITY_TEMPLET) ?
                params.getString(ConfigConstants.FIELD_ENTITY_TEMPLET) : "/templates/entity.java.vm");
        //控制层模板
        tc.setController(params.containsKey(ConfigConstants.FIELD_CONTROLLER_TEMPLET) ?
                params.getString(ConfigConstants.FIELD_CONTROLLER_TEMPLET) : "/templates/controller.java.vm");
        //服务层模板
        tc.setService(params.containsKey(ConfigConstants.FIELD_SERVICE_TEMPLET) ?
                params.getString(ConfigConstants.FIELD_SERVICE_TEMPLET) : "/templates/service.java.vm");
        //实现层模板
        tc.setServiceImpl(params.containsKey(ConfigConstants.FIELD_IMPL_TEMPLET) ?
                params.getString(ConfigConstants.FIELD_IMPL_TEMPLET) : "/templates/serviceImpl.java.vm");
        //sql层模板
        tc.setXml(params.containsKey(ConfigConstants.FIELD_MAPPER_TEMPLET) ?
                params.getString(ConfigConstants.FIELD_MAPPER_TEMPLET) : "/templates/mapper.xml.vm");
        //dao层模板
        tc.setMapper(params.containsKey(ConfigConstants.FIELD_DAO_TEMPLET) ?
                params.getString(ConfigConstants.FIELD_DAO_TEMPLET) : "/templates/mapper.java.vm");
        //实体类
        if (params.containsKey(ConfigConstants.FIELD_MODEL_TEMPLET)) {
            tc.setEntity(params.getString(ConfigConstants.FIELD_MODEL_TEMPLET));
        }
        //实体类外键
        if (params.containsKey(ConfigConstants.FIELD_MODEL_FKEY_TEMPLET)) {
            tc.setEntityKt(params.getString(ConfigConstants.FIELD_MODEL_FKEY_TEMPLET));
        }
        // 如上任何一个模块如果设置 空 OR Null 将不生成该模块。
        generator.setTemplate(tc);
        // 执行生成
        generator.execute();
        //返回处理成功
        return true;
    }

    /**
     * 校验必填字段
     *
     * @param params 请求参数
     * @param fields 必填字段
     * @return true为真
     * @author Mr.Yang
     * @date 2018/12/5
     */
    private static boolean checkParam(JSONObject params, String... fields) {
        // 校验请求参数
        for (String key : fields) {
            // 判断是否存在该字段、字段是否为空
            if (!params.containsKey(key) || StringUtils.isEmpty(params.getString(key))) {
                return false;
            }
        }
        return true;
    }


    /**
     * 包配置
     *
     * @param convert   转换配置信息
     * @param generator 创建实例
     * @author Mr.Yang
     * @date 2018/11/29 0029
     */
    private static void packageInfo(AutoGenerator generator, JSONObject convert) {
        //包配置信息
        generator.setPackageInfo(new PackageConfig()
                // 自定义包路径
                .setParent(convert.getString(ConfigConstants.FIELD_BASE_PACKAGE))
                // 这里是控制器包名，默认 web
                .setController(convert.containsKey(ConfigConstants.FIELD_CONTROLLER_NAME) ?
                        convert.getString(ConfigConstants.FIELD_CONTROLLER_NAME) : "controller")
                // 设置Entity包名，默认entity
                .setEntity(convert.containsKey(ConfigConstants.FIELD_MODEL_NAME) ?
                        convert.getString(ConfigConstants.FIELD_MODEL_NAME) : "model")
                // 设置Mapper包名，默认mapper
                .setMapper(convert.containsKey(ConfigConstants.FIELD_DAO_NAME) ?
                        convert.getString(ConfigConstants.FIELD_DAO_NAME) : "dao")
                // 设置Service包名，默认service
                .setService(convert.containsKey(ConfigConstants.FIELD_SERVICE_NAME) ?
                        convert.getString(ConfigConstants.FIELD_SERVICE_NAME) : "service")
                // 设置Service Impl包名，默认service.impl
                .setServiceImpl(convert.containsKey(ConfigConstants.FIELD_IMPL_NAME) ?
                        convert.getString(ConfigConstants.FIELD_IMPL_NAME) : "service.impl")
                // 设置Mapper XML包名，默认mapper.xml
                .setXml(convert.containsKey(ConfigConstants.FIELD_MAPPER_NAME) ?
                        convert.getString(ConfigConstants.FIELD_MAPPER_NAME) : "mapper")
        );
    }

    /**
     * 策略模式
     *
     * @param convert   转换配置信息
     * @param generator 创建实例
     * @author Mr.Yang
     * @date 2018/11/29 0029
     */
    private static void strategyConfig(AutoGenerator generator, JSONObject convert) {
        //表名生成策略
        NamingStrategy namingStrategy = NamingStrategy.underline_to_camel;
        //不是驼峰命名
        if (convert.containsKey(ConfigConstants.FIELD_NO_CHANGE)) {
            namingStrategy = NamingStrategy.no_change;
        }
        StrategyConfig strategyConfig = new StrategyConfig();
        //策略配置
        strategyConfig
                // 全局大写命名
                .setCapitalMode(convert.containsKey(ConfigConstants.FIELD_CAPITAL_MODE))
                // 此处可以修改为您的表前缀
                .setTablePrefix(convert.getString(ConfigConstants.FIELD_PREFIX).split(","))
                // 表名生成策略
                .setNaming(namingStrategy)
                // 需要生成的表
                .setInclude(convert.getString(ConfigConstants.FIELD_INCLUDE_TABLES).split(","))
                .setRestControllerStyle(true).setControllerMappingHyphenStyle(true)
                //是否启用lombok
                .setEntityLombokModel(
                        convert.containsKey(ConfigConstants.FIELD_ENTITY_LOMBOK) ?
                                convert.getBoolean(ConfigConstants.FIELD_ENTITY_LOMBOK) : true);
        // 排除生成的表
        if (convert.containsKey(ConfigConstants.FIELD_EXCLUDE_TABLES)) {
            strategyConfig.setExclude(convert.getString(ConfigConstants.FIELD_EXCLUDE_TABLES).split(","));
        }
        // 自定义实体父类
        if (convert.containsKey(ConfigConstants.FIELD_SUPER_ENTITY)) {
            strategyConfig.setSuperEntityClass(convert.getString(ConfigConstants.FIELD_SUPER_ENTITY));
        }
        //  自定义 mapper 父类 默认BaseMapper
        if (convert.containsKey(ConfigConstants.FIELD_SUPER_MAPPER)) {
            strategyConfig.setSuperMapperClass(convert.getString(ConfigConstants.FIELD_SUPER_MAPPER));
        }
        // 自定义 service 父类 默认IService
        if (convert.containsKey(ConfigConstants.FIELD_SUPER_SERVICE)) {
            strategyConfig.setSuperEntityClass(convert.getString(ConfigConstants.FIELD_SUPER_SERVICE));
        }
        // 自定义 service 实现类父类 默认ServiceImpl
        if (convert.containsKey(ConfigConstants.FIELD_SUPER_SERVICE_IMPL)) {
            strategyConfig.setSuperServiceImplClass(convert.getString(ConfigConstants.FIELD_SUPER_SERVICE_IMPL));
        }
        // 自定义父类controller
        if (convert.containsKey(ConfigConstants.FIELD_SUPER_CONTROLLER)) {
            strategyConfig.setSuperControllerClass(convert.containsKey(ConfigConstants.FIELD_SUPER_CONTROLLER) ?
                    convert.getString(ConfigConstants.FIELD_SUPER_CONTROLLER) : ConfigConstants.BASE_REQUEST_RESPONSE);
        }
        // Boolean类型字段是否移除is前缀处理
        if (convert.containsKey(ConfigConstants.FIELD_ENTITY_IS_PREFIX)) {
            strategyConfig.setEntityBooleanColumnRemoveIsPrefix(
                    convert.containsKey(ConfigConstants.FIELD_ENTITY_IS_PREFIX));
        }
        generator.setStrategy(strategyConfig);
    }

    /**
     * 全局配置服务
     *
     * @param convert   转换配置信息
     * @param generator 创建实例
     * @author Mr.Yang
     * @date 2018/11/29 0029
     */
    private static void globalConfig(AutoGenerator generator, JSONObject convert) {
        DataSourceConfig sourceConfig = new DataSourceConfig();
        //设置连接信息
        getDbType(convert, sourceConfig);
        //是否启用activeRecord
        boolean activeFlag = convert.containsKey(ConfigConstants.FIELD_ACTIVE_FLAG);
        //获取项目路径
        String projectPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
        //判断操作系统
        if (System.getProperty(ConfigConstants.OS_NAME).toLowerCase().contains(ConfigConstants.WINDOWS)) {
            //处理前置路径
            String preposePath = projectPath.substring(1);
            //处理
            projectPath = preposePath.substring(0, preposePath.length() - "target/classes/".length()).replaceAll("/", "\\\\");
        } else {
            //如果存在file开头，截取掉
            if (projectPath.startsWith(ConfigConstants.FIELD_FILE)) {
                projectPath = projectPath.substring(5);
            }
        }
        //设置数据连接
        generator.setDataSource(sourceConfig).setGlobalConfig(new GlobalConfig()
                //输出目录
                .setOutputDir(convert.containsKey(ConfigConstants.FIELD_OUT_PATH) ? convert.getString(ConfigConstants.FIELD_OUT_PATH)
                        : projectPath + "src/main/java")
                // 是否覆盖文件
                .setFileOverride(true)
                //开启Swagger2
                .setSwagger2(convert.containsKey(ConfigConstants.FIELD_SWAGGER) ?
                        convert.getBoolean(ConfigConstants.FIELD_SWAGGER) : false)
                // 开启 activeRecord 模式
                .setActiveRecord(activeFlag)
                // XML ResultMap
                .setBaseResultMap(true)
                // XML columList
                .setBaseColumnList(true)
                //生成后打开文件夹
                .setOpen(false)
                .setAuthor(convert.containsKey(ConfigConstants.FIELD_AUTHOR) ?
                        convert.getString(ConfigConstants.FIELD_AUTHOR) : "administrator")
                // 自定义文件命名，注意 %s 会自动填充表实体属性！
                .setMapperName("%sMapper")
                .setXmlName("%sMapper")
                .setServiceName("%sService")
                .setServiceImplName("%sServiceImpl")
                .setControllerName("%sController")
        );
        //设置策略模式
        strategyConfig(generator, convert);
        //包文件配置
        packageInfo(generator, convert);
    }


    /**
     * 判断数据库类型
     *
     * @param convert      转换配置信息
     * @param sourceConfig 数据库配置
     * @author Mr.Yang
     * @date 2018/11/29 0029
     */
    private static void getDbType(JSONObject convert, DataSourceConfig sourceConfig) {
        //获取连接url
        String url = convert.getString(ConfigConstants.JDBC_URL);
        //判断数据库类型
        if (url.contains(ConfigConstants.FIELD_MYSQL)) {
            sourceConfig.setDbType(DbType.MYSQL).setDriverName(ConfigEnum.MYSQL_JDBC.getValue());
        } else if (url.contains(ConfigConstants.FIELD_ORACLE)) {
            sourceConfig.setDbType(DbType.ORACLE).setDriverName(ConfigEnum.ORACLE_JDBC.getValue());
        } else if (url.contains(ConfigConstants.FIELD_POSTGRESQL)) {
            sourceConfig.setDbType(DbType.POSTGRE_SQL).setDriverName(ConfigEnum.POSTGRESQL_JDBC.getValue());
        } else if (url.contains(ConfigConstants.FIELD_DB2)) {
            sourceConfig.setDbType(DbType.DB2).setDriverName(ConfigEnum.DB2_JDBC.getValue());
        } else if (url.contains(ConfigConstants.FIELD_MARIADB)) {
            sourceConfig.setDbType(DbType.MARIADB).setDriverName(ConfigEnum.MARIADB_JDBC.getValue());
        } else {
            throw ExceptionUtils.mpe("Unknown type of database!");
        }
        //设置数据库连接信息
        sourceConfig.setUrl(url)
                .setUsername(convert.getString(ConfigConstants.JDBC_USER_NAME))
                .setPassword(convert.getString(ConfigConstants.JDBC_USER_PASSWORD));
    }

    /**
     * 将集合写入文件中
     *
     * @param file     文件路径
     * @param contents 列表集合
     * @return 写入成功返回true
     * @author Mr.Yang
     * @date 2018/12/2
     */
    private static boolean writeLines(File file, List<String> contents) {
        //写入操作
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < contents.size(); i++) {
                if (i > 0) {
                    writer.newLine();
                }
                writer.append(contents.get(i));
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        }
    }

}
