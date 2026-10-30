package com.hxf.exambackend01.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxf.exambackend01.entity.QuestionCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionCategoryMapper extends BaseMapper<QuestionCategory> {
}