package com.lcb.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcb.system.domain.SysUser;
import com.lcb.system.mapper.SysUserMapper;
import com.lcb.system.service.ISysUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean save(SysUser entity) {
        if (entity.getPassword() != null && !entity.getPassword().isEmpty()) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(SysUser entity) {
        if (entity.getPassword() != null && !entity.getPassword().isEmpty()) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
        return super.updateById(entity);
    }
}
