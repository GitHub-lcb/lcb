package com.lcb.generator.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lcb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_gen_table_column")
public class GenTableColumn extends BaseEntity {
    private Long id;
    private Long tableId;
    private String columnName;
    private String columnComment;
    private String javaType;
    private String javaField;
    private Integer isInsert;
    private Integer isEdit;
    private Integer isList;
    private String queryType;
}
