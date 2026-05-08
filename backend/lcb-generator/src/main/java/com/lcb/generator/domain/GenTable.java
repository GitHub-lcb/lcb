package com.lcb.generator.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lcb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_gen_table")
public class GenTable extends BaseEntity {
    private Long id;
    private String tableName;
    private String tableComment;
    private String className;
    private String moduleName;
    private String packageName;
    private String tplCategory;
}
