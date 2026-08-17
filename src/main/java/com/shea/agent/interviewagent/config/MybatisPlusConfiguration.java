package com.shea.agent.interviewagent.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

/**
 * @author : Shea.
 * @since : 2026/8/17 09:55
 */
@Configuration
public class MybatisPlusConfiguration {

    public static class AutoFieldFillHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            // 自动填充创建时间字段
            strictInsertFill(metaObject,"createTime", LocalDate.class, LocalDate.now());
            // 自动填充修改时间字段
            strictInsertFill(metaObject,"updateTime", LocalDate.class, LocalDate.now());
            // 自动填充逻辑删除字段deleted:0未删除
            strictInsertFill(metaObject,"isDeleted",Integer.class,0);
            // 自动填充状态:0 正常

        }

        @Override
        public void updateFill(MetaObject metaObject) {
            // 自动填充修改时间字段
            strictInsertFill(metaObject,"updateTime", LocalDate.class, LocalDate.now());
        }
    }

    @Bean
    public AutoFieldFillHandler autoFieldFillHandler() {
        return new AutoFieldFillHandler();
    }
}
