package com.lcb.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lcb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_user")
public class SysUser extends BaseEntity {
    private Long id;
    private String username;

    @JsonIgnore
    private String password;

    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
}
