package com.lcb.system.vo;

import com.lcb.system.domain.SysDictType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysDictTypeVO {
    private Long id;
    private String dictName;
    private String dictType;
    private Integer status;
    private LocalDateTime createTime;

    public static SysDictTypeVO fromEntity(SysDictType dictType) {
        if (dictType == null) return null;
        SysDictTypeVO vo = new SysDictTypeVO();
        vo.setId(dictType.getId());
        vo.setDictName(dictType.getDictName());
        vo.setDictType(dictType.getDictType());
        vo.setStatus(dictType.getStatus());
        vo.setCreateTime(dictType.getCreateTime());
        return vo;
    }
}
