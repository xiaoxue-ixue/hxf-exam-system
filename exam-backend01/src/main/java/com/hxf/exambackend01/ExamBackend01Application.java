package com.hxf.exambackend01;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hxf.exambackend01.mapper")
public class ExamBackend01Application {

    public static void main(String[] args) {
        SpringApplication.run(ExamBackend01Application.class, args);
    }

}
