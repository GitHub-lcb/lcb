package com.lcb.system.dto;

import lombok.Data;

@Data
public class SysRoleDTO {
    private Long id;
    private String roleName;
    private String roleKey;
    private Integer dataScope;
    private Integer status;
}
