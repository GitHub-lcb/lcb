package com.lcb.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lcb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_role")
public class SysRole extends BaseEntity {
    private Long id;
    private String roleName;
    private String roleKey;
    private Integer dataScope;
    private Integer status;
}
