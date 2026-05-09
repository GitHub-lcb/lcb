package com.lcb.system.vo;

import com.lcb.system.domain.SysMenu;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SysMenuVO {
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
    private List<SysMenuVO> children;

    public static SysMenuVO fromEntity(SysMenu menu) {
        if (menu == null) return null;
        SysMenuVO vo = new SysMenuVO();
        vo.setId(menu.getId());
        vo.setMenuName(menu.getMenuName());
        vo.setPermission(menu.getPermission());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setIcon(menu.getIcon());
        vo.setParentId(menu.getParentId());
        vo.setSort(menu.getSort());
        vo.setMenuType(menu.getMenuType());
        vo.setStatus(menu.getStatus());
        if (menu.getChildren() != null) {
            vo.setChildren(menu.getChildren().stream()
                .map(SysMenuVO::fromEntity)
                .collect(Collectors.toList()));
        }
        return vo;
    }
}
