package com.lcb.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcb.system.domain.SysRole;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {
    @Select("SELECT r.role_key FROM t_sys_role r " +
            "JOIN t_sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.del_flag = 0 AND r.status = 1")
    List<String> selectRoleKeysByUserId(Long userId);
}
