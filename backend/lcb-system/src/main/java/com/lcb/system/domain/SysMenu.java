package com.lcb.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lcb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_menu")
public class SysMenu extends BaseEntity {
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

    @TableField(exist = false)
    private List<SysMenu> children;
}
