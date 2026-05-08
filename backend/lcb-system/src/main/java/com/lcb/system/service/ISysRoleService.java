package com.lcb.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lcb.system.domain.SysRole;
import java.util.List;

public interface ISysRoleService extends IService<SysRole> {
    List<String> selectRoleKeysByUserId(Long userId);
}
