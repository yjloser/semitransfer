package com.semitransfer.plus.config.internal;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 填充器
 *
 * @program: semitransfer
 * @author: Mr.Yang
 * @date: 2018-12-07 23:54
 * @version:V2.0
 **/
@Component
public class FillMetaObjectHandler implements MetaObjectHandler {
    /**
     * 新增填充
     *
     * @param metaObject 元数据
     * @author Mr.Yang
     * @date 2018/12/7
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        //获取所有时间
        Arrays.asList(metaObject.getSetterNames()).forEach(datetime -> {
            //以time结尾并未设置值
            if (datetime.endsWith("Time") && null == metaObject.getValue(datetime)) {
                this.setFieldValByName(datetime, LocalDateTime.now(), metaObject);
            }
        });
    }

    /**
     * 更新填充
     *
     * @param metaObject 元数据
     * @author Mr.Yang
     * @date 2018/12/7
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        insertFill(metaObject);
    }
}
