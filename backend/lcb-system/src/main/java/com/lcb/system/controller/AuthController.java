package com.lcb.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lcb.common.core.Result;
import com.lcb.common.exception.ServiceException;
import com.lcb.system.domain.LoginDTO;
import com.lcb.system.domain.SysUser;
import com.lcb.system.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserMapper userMapper;

    public AuthController(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        SysUser user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername())
        );
        if (user == null) {
            throw new ServiceException("用户名或密码错误");
        }
        // 生产环境应使用 BCrypt 密码校验
        if (!dto.getPassword().equals(user.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        StpUtil.login(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        result.put("user", user);
        return Result.ok(result);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    @Operation(summary = "当前用户信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        List<String> permissions = StpUtil.getPermissionList();
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("permissions", permissions);
        return Result.ok(result);
    }
}
