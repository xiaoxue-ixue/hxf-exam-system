package com.hxf.exambackend01.dto;

import lombok.Data;

@Data
public class UserSaveDTO {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private Integer status;
    private Long roleId;
}