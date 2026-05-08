package com.lcb.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcb.system.domain.SysMenu;
import com.lcb.system.mapper.SysMenuMapper;
import com.lcb.system.service.ISysMenuService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {
    @Override
    public List<String> selectMenuPermsByUserId(Long userId) {
        return baseMapper.selectMenuPermsByUserId(userId);
    }
}
