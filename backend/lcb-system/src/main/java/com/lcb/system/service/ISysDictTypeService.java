package com.lcb.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lcb.system.domain.SysDictType;
import com.lcb.system.domain.SysDictData;
import java.util.List;

public interface ISysDictTypeService extends IService<SysDictType> {
    List<SysDictData> selectDataByType(String dictType);
}
