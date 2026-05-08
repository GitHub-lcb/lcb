package com.lcb.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcb.generator.domain.GenTable;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface GenTableMapper extends BaseMapper<GenTable> {

    @Select("SELECT table_name, table_comment FROM information_schema.tables " +
            "WHERE table_schema = (SELECT DATABASE()) AND table_name NOT LIKE 't_%' AND table_name NOT LIKE 'gen_%'")
    List<java.util.Map<String, Object>> selectDbTables();

    @Select("SELECT column_name, column_comment, data_type, " +
            "IF(IS_NULLABLE = 'YES', 'YES', 'NO') as nullable, " +
            "IF(COLUMN_KEY = 'PRI', 'PRI', '') as column_key, " +
            "COALESCE(CHARACTER_MAXIMUM_LENGTH, 0) as char_length " +
            "FROM information_schema.columns " +
            "WHERE table_schema = (SELECT DATABASE()) AND table_name = #{tableName} " +
            "ORDER BY ordinal_position")
    List<java.util.Map<String, Object>> selectDbColumns(String tableName);
}
