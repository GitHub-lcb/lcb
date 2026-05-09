package com.lcb.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoleMenuDTO {
    private Long roleId;
    private List<Long> menuIds;
}
