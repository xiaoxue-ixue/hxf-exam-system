package com.hxf.exambackend01.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxf.exambackend01.common.BusinessException;
import com.hxf.exambackend01.dto.LoginDTO;
import com.hxf.exambackend01.entity.SysRole;
import com.hxf.exambackend01.entity.SysUser;
import com.hxf.exambackend01.entity.SysUserRole;
import com.hxf.exambackend01.mapper.SysRoleMapper;
import com.hxf.exambackend01.mapper.SysUserMapper;
import com.hxf.exambackend01.mapper.SysUserRoleMapper;
import com.hxf.exambackend01.service.AuthService;
import com.hxf.exambackend01.util.JwtUtil;
import com.hxf.exambackend01.vo.LoginVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(SysUserMapper sysUserMapper,
                           SysUserRoleMapper sysUserRoleMapper,
                           SysRoleMapper sysRoleMapper,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 按用户名查用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) throw new BusinessException("账号或密码错误");
        // 2. 校验账号状态：被禁用的账号不允许登录
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        // 3. 校验密码：优先 BCrypt，兼容第 2 周明文密码并自动升级
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            if (!dto.getPassword().equals(user.getPassword())) {
                throw new BusinessException("账号或密码错误");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            sysUserMapper.updateById(user);
        }
        // 4. 解析角色并签发 token
        String role = resolveRole(user.getId());
        String token = jwtUtil.generateToken(user.getUsername(), role);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(role);
        return vo;
    }

    /** 用户 → sys_user_role → sys_role 解析角色编码 */
    private String resolveRole(Long userId) {
        SysUserRole ur = sysUserRoleMapper.selectOne(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (ur == null) {
            throw new BusinessException("该用户未分配角色");
        }
        SysRole role = sysRoleMapper.selectById(ur.getRoleId());
        return role == null ? null : role.getRoleCode();
    }
}