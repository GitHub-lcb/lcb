package com.lcb.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcb.system.domain.SysRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {
    @Select("SELECT r.role_key FROM t_sys_role r " +
            "JOIN t_sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.del_flag = 0 AND r.status = 1")
    List<String> selectRoleKeysByUserId(Long userId);

    @Delete("DELETE FROM t_sys_role_menu WHERE role_id = #{roleId}")
    void deleteRoleMenuByRoleId(@Param("roleId") Long roleId);

    @Insert("<script>" +
            "INSERT INTO t_sys_role_menu (role_id, menu_id) VALUES " +
            "<foreach collection='menuIds' item='menuId' separator=','>" +
            "(#{roleId}, #{menuId})" +
            "</foreach>" +
            "</script>")
    void insertRoleMenu(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}
