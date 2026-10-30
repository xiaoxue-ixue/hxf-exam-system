package com.hxf.exambackend01.controller;

import com.hxf.exambackend01.common.R;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public R<String> hello(){
        return R.success("hello,在线考试系统后端启动成功！");
    }
}
