package com.lcb.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lcb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_dict_data")
public class SysDictData extends BaseEntity {
    private Long id;
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private Integer dictSort;
    private String cssClass;
    private Integer status;
}
