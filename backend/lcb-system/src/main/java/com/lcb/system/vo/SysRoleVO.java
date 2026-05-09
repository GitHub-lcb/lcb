package com.lcb.system.vo;

import com.lcb.system.domain.SysRole;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysRoleVO {
    private Long id;
    private String roleName;
    private String roleKey;
    private Integer dataScope;
    private Integer status;
    private LocalDateTime createTime;

    public static SysRoleVO fromEntity(SysRole role) {
        if (role == null) return null;
        SysRoleVO vo = new SysRoleVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleKey(role.getRoleKey());
        vo.setDataScope(role.getDataScope());
        vo.setStatus(role.getStatus());
        vo.setCreateTime(role.getCreateTime());
        return vo;
    }
}
