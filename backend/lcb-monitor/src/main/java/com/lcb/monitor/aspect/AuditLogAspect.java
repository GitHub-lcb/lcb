package com.lcb.monitor.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.lcb.monitor.domain.SysAuditLog;
import com.lcb.monitor.service.IAuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {

    private final IAuditLogService auditLogService;

    public AuditLogAspect(IAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("execution(* com.lcb.*.controller.*.*(..)) && " +
            "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            " @annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            " @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();

        SysAuditLog log = new SysAuditLog();
        try {
            long userId = StpUtil.getLoginIdAsLong();
            log.setUsername(auditLogService.resolveUsername(userId));
        } catch (Exception e) {
            log.setUsername("anonymous");
        }
        log.setOperation(signature.getMethod().getName());
        log.setMethod(signature.getDeclaringTypeName() + "." + signature.getMethod().getName());

        try {
            Object result = pjp.proceed();
            log.setDuration(System.currentTimeMillis() - start);
            log.setStatus(1);
            return result;
        } catch (Throwable e) {
            log.setDuration(System.currentTimeMillis() - start);
            log.setStatus(0);
            throw e;
        } finally {
            auditLogService.save(log);
        }
    }
}
