package com.hxf.exambackend01.vo;

import lombok.Data;

import java.util.List;

/**
 * 分类树 VO：含 children 字段供前端 el-tree 使用。
 */
@Data
public class CategoryVO {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sort;
    /** 子节点；叶子节点为空数组而不是 null */
    private List<CategoryVO> children;
}