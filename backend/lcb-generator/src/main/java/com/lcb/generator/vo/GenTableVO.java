package com.lcb.generator.vo;

import com.lcb.generator.domain.GenTable;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GenTableVO {
    private Long id;
    private String tableName;
    private String tableComment;
    private String className;
    private String moduleName;
    private String packageName;
    private String tplCategory;
    private LocalDateTime createTime;

    public static GenTableVO fromEntity(GenTable table) {
        if (table == null) return null;
        GenTableVO vo = new GenTableVO();
        vo.setId(table.getId());
        vo.setTableName(table.getTableName());
        vo.setTableComment(table.getTableComment());
        vo.setClassName(table.getClassName());
        vo.setModuleName(table.getModuleName());
        vo.setPackageName(table.getPackageName());
        vo.setTplCategory(table.getTplCategory());
        vo.setCreateTime(table.getCreateTime());
        return vo;
    }
}
