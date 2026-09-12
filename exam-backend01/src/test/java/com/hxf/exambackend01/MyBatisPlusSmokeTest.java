package com.hxf.exambackend01;

import com.hxf.exambackend01.entity.SysUser;
import com.hxf.exambackend01.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 冒烟测试：验证 MyBatis-Plus 在 Spring Boot 3 下自动配置是否真正生效。
 * 三项都要过：数据源能连、Mapper 能被扫到、@TableLogic 生效。
 */
@SpringBootTest
class MyBatisPlusSmokeTest {

    @Autowired
    private SysUserMapper sysUserMapper;

    /** 1) Mapper 被扫到 + 数据源连通 + BaseMapper 可用 */
    @Test
    void baseMapper_selectById() {
        SysUser u = sysUserMapper.selectById(1L);
        assertNotNull(u, "查不到 id=1，说明自动配置或数据源有问题");
        assertEquals("admin", u.getUsername());
        assertEquals("系统管理员", u.getRealName());
    }

    /** 2) @TableLogic 生效：SQL 应自动追加 deleted=0 */
    @Test
    void tableLogic_appendsDeletedCondition() {
        List<SysUser> list = sysUserMapper.selectList(null);
        assertFalse(list.isEmpty());
        list.forEach(u -> assertNotEquals(1, u.getDeleted(), "逻辑删除的行不该被查出来"));
    }
}
