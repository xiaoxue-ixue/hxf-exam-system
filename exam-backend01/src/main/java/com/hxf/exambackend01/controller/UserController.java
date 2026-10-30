package com.hxf.exambackend01.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxf.exambackend01.common.R;
import com.hxf.exambackend01.dto.UserQueryDTO;
import com.hxf.exambackend01.dto.UserSaveDTO;
import com.hxf.exambackend01.entity.SysRole;
import com.hxf.exambackend01.mapper.SysRoleMapper;
import com.hxf.exambackend01.service.UserService;
import com.hxf.exambackend01.vo.UserVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final SysRoleMapper sysRoleMapper;

    public UserController(UserService userService, SysRoleMapper sysRoleMapper) {
        this.userService = userService;
        this.sysRoleMapper = sysRoleMapper;
    }

    /** 分页查询用户列表 */
    @GetMapping("/page")
    public R<Page<UserVO>> page(UserQueryDTO query) {
        return R.success(userService.page(query));
    }

    /** 角色列表（供新增/编辑下拉选择） */
    @GetMapping("/roles")
    public R<List<SysRole>> roles() {
        return R.success(sysRoleMapper.selectList(null));
    }

    /** 新增用户 */
    @PostMapping
    public R<Void> add(@RequestBody UserSaveDTO dto) {
        userService.save(dto);
        return R.success();
    }

    /** 编辑用户 */
    @PutMapping
    public R<Void> update(@RequestBody UserSaveDTO dto) {
        userService.update(dto);
        return R.success();
    }

    /** 删除用户（逻辑删除） */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return R.success();
    }

    /** 启用 / 禁用 */
    @PutMapping("/{id}/status/{status}")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable Integer status) {
        userService.changeStatus(id, status);
        return R.success();
    }

    /** 重置密码 */
    @PutMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return R.success();
    }
}