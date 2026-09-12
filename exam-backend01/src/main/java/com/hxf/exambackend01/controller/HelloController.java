package com.hxf.exambackend01.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello(){
        return "Hello, 在线考试系统后端启动成功!";
    }
}
