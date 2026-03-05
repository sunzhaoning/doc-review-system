package com.docreview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 文档评审系统启动类
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@SpringBootApplication
@MapperScan("com.docreview.mapper")
public class DocReviewApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DocReviewApplication.class, args);
    }
}
