package com.hxf.exambackend01.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxf.exambackend01.dto.UserQueryDTO;
import com.hxf.exambackend01.dto.UserSaveDTO;
import com.hxf.exambackend01.vo.UserVO;

public interface UserService {
    Page<UserVO> page(UserQueryDTO query);
    void save(UserSaveDTO dto);
    void update(UserSaveDTO dto);
    void delete(Long id);
    void changeStatus(Long id, Integer status);
    void resetPassword(Long id);
}