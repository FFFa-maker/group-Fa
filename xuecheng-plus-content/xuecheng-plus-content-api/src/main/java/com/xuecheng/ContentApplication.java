package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import com.xuecheng.content.config.MybatisPlusConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@SpringBootApplication
@EnableSwagger2Doc
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }
}
