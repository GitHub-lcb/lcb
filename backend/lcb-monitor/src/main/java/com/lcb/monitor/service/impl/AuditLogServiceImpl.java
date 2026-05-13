package com.lcb.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcb.monitor.domain.SysAuditLog;
import com.lcb.monitor.mapper.SysAuditLogMapper;
import com.lcb.monitor.service.IAuditLogService;
import com.lcb.system.domain.SysUser;
import com.lcb.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl extends ServiceImpl<SysAuditLogMapper, SysAuditLog> implements IAuditLogService {

    private final SysUserMapper sysUserMapper;

    public AuditLogServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public String resolveUsername(Long userId) {
        if (userId == null) return "anonymous";
        SysUser user = sysUserMapper.selectById(userId);
        return user != null ? user.getUsername() : String.valueOf(userId);
    }
}
