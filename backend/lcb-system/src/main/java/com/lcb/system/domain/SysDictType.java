package com.lcb.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lcb.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_dict_type")
public class SysDictType extends BaseEntity {
    private Long id;
    private String dictName;
    private String dictType;
    private Integer status;
}
