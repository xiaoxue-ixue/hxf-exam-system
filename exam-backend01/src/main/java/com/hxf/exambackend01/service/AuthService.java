package com.hxf.exambackend01.service;

import com.hxf.exambackend01.dto.LoginDTO;
import com.hxf.exambackend01.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO dto);
}