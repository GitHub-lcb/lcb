package com.lcb.monitor.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.lcb.monitor.domain.SysAuditLog;
import com.lcb.monitor.mapper.SysAuditLogMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {

    private final SysAuditLogMapper auditLogMapper;

    public AuditLogAspect(SysAuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Around("execution(* com.lcb.*.controller.*.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long duration = System.currentTimeMillis() - start;

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        SysAuditLog log = new SysAuditLog();
        log.setUsername(StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : "anonymous");
        log.setOperation(signature.getMethod().getName());
        log.setMethod(signature.getDeclaringTypeName() + "." + signature.getMethod().getName());
        log.setDuration(duration);
        log.setStatus(1);
        auditLogMapper.insert(log);

        return result;
    }
}
