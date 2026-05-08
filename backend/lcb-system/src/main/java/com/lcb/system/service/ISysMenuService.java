package com.lcb.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lcb.system.domain.SysMenu;
import java.util.List;

public interface ISysMenuService extends IService<SysMenu> {
    List<String> selectMenuPermsByUserId(Long userId);
}
