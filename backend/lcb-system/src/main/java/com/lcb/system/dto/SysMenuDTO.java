package com.lcb.system.dto;

import lombok.Data;

@Data
public class SysMenuDTO {
    private Long id;
    private String menuName;
    private String permission;
    private String path;
    private String component;
    private String icon;
    private Long parentId;
    private Integer sort;
    private String menuType;
    private Integer status;
}
