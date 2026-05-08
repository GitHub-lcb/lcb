package com.lcb.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcb.system.domain.SysDictData;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface SysDictDataMapper extends BaseMapper<SysDictData> {
    @Select("SELECT * FROM t_sys_dict_data WHERE dict_type = #{dictType} AND status = 1 AND del_flag = 0 ORDER BY dict_sort")
    List<SysDictData> selectByType(String dictType);
}
