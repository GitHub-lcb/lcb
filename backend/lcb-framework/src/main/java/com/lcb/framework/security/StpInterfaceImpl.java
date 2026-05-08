package com.lcb.framework.security;

import cn.dev33.satoken.stp.StpInterface;
import com.lcb.system.service.ISysMenuService;
import com.lcb.system.service.ISysRoleService;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    private final ISysRoleService roleService;
    private final ISysMenuService menuService;

    public StpInterfaceImpl(ISysRoleService roleService, ISysMenuService menuService) {
        this.roleService = roleService;
        this.menuService = menuService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return menuService.selectMenuPermsByUserId(Long.valueOf(loginId.toString()));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return roleService.selectRoleKeysByUserId(Long.valueOf(loginId.toString()));
    }
}
