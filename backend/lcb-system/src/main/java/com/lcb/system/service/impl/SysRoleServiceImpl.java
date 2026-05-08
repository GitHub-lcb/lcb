package com.lcb.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcb.system.domain.SysRole;
import com.lcb.system.mapper.SysRoleMapper;
import com.lcb.system.service.ISysRoleService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    @Override
    public List<String> selectRoleKeysByUserId(Long userId) {
        return baseMapper.selectRoleKeysByUserId(userId);
    }
}
