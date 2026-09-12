package com.hxf.exambackend01.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxf.exambackend01.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
