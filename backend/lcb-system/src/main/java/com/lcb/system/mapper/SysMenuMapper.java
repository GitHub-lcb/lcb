package com.lcb.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcb.system.domain.SysMenu;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {
    @Select("SELECT DISTINCT m.permission FROM t_sys_menu m " +
            "JOIN t_sys_role_menu rm ON m.id = rm.menu_id " +
            "JOIN t_sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.permission IS NOT NULL " +
            "AND m.del_flag = 0 AND m.status = 1")
    List<String> selectMenuPermsByUserId(Long userId);
}
