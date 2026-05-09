package com.lcb.system.dto;

import lombok.Data;

@Data
public class SysUserDTO {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
}
