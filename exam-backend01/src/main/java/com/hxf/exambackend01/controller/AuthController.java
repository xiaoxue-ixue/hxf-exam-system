package com.hxf.exambackend01.controller;

import com.hxf.exambackend01.common.R;
import com.hxf.exambackend01.dto.LoginDTO;
import com.hxf.exambackend01.service.AuthService;
import com.hxf.exambackend01.vo.LoginVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody LoginDTO dto) {
        return R.success(authService.login(dto));
    }
}