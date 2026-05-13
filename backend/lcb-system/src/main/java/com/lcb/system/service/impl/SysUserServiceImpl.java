package com.lcb.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcb.system.domain.SysUser;
import com.lcb.system.mapper.SysUserMapper;
import com.lcb.system.service.ISysUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public boolean save(SysUser entity) {
        if (entity.getPassword() == null || entity.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        return super.save(entity);
    }

    @Override
    @Transactional
    public boolean updateById(SysUser entity) {
        if (entity.getPassword() != null) {
            if (entity.getPassword().length() < 6) {
                throw new IllegalArgumentException("密码长度不能少于6位");
            }
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
        return super.updateById(entity);
    }
}
