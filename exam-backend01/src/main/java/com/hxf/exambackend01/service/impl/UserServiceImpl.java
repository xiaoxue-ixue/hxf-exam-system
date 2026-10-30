package com.hxf.exambackend01.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxf.exambackend01.common.BusinessException;
import com.hxf.exambackend01.dto.UserQueryDTO;
import com.hxf.exambackend01.dto.UserSaveDTO;
import com.hxf.exambackend01.entity.SysRole;
import com.hxf.exambackend01.entity.SysUser;
import com.hxf.exambackend01.entity.SysUserRole;
import com.hxf.exambackend01.mapper.SysRoleMapper;
import com.hxf.exambackend01.mapper.SysUserMapper;
import com.hxf.exambackend01.mapper.SysUserRoleMapper;
import com.hxf.exambackend01.service.UserService;
import com.hxf.exambackend01.vo.UserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(SysUserMapper sysUserMapper,
                           SysRoleMapper sysRoleMapper,
                           SysUserRoleMapper sysUserRoleMapper,
                           PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<UserVO> page(UserQueryDTO query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getRealName()), SysUser::getRealName, query.getRealName())
                .orderByDesc(SysUser::getId);
        Page<SysUser> userPage = sysUserMapper.selectPage(page, wrapper);

        // 泛型要从 SysUser 换成 UserVO，新建 Page 时 total 必须手动搬过去
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> records = userPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        SysUserRole userRole = sysUserRoleMapper.selectOne(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        if (userRole != null) {
            SysRole role = sysRoleMapper.selectById(userRole.getRoleId());
            if (role != null) {
                vo.setRoleCode(role.getRoleCode());
                vo.setRoleName(role.getRoleName());
            }
        }
        return vo;
    }

    @Override
    public void save(UserSaveDTO dto) {
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        sysUserMapper.insert(user);
        // insert 后 MP 已把自增主键回填到 user，直接用 user.getId()
        saveUserRole(user.getId(), dto.getRoleId());
    }

    private void saveUserRole(Long userId, Long roleId) {
        if (roleId == null) {
            return;
        }
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        sysUserRoleMapper.insert(userRole);
    }

    @Override
    public void update(UserSaveDTO dto) {
        SysUser user = sysUserMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setRealName(dto.getRealName());
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        sysUserMapper.updateById(user);
        // 角色更新：先删旧关联，再插新关联（本篇最重要知识点）
        if (dto.getRoleId() != null) {
            sysUserRoleMapper.delete(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, dto.getId()));
            saveUserRole(dto.getId(), dto.getRoleId());
        }
    }

    @Override
    public void delete(Long id) {
        // @TableLogic 生效后，实际执行 UPDATE ... SET deleted=1
        sysUserMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        sysUserMapper.updateById(user);
    }
}