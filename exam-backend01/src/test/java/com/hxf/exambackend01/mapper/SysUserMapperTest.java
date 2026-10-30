package com.hxf.exambackend01.mapper;

import com.hxf.exambackend01.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class SysUserMapperTest {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Test
    void testSelectList() {
        List<SysUser> users = sysUserMapper.selectList(null);
        users.forEach(u -> System.out.println(u.getId()
                + "-" + u.getUsername() + "-" + u.getRealName()));
    }
}