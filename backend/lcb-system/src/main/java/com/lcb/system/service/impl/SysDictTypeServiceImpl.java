package com.lcb.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcb.system.domain.SysDictData;
import com.lcb.system.domain.SysDictType;
import com.lcb.system.mapper.SysDictDataMapper;
import com.lcb.system.mapper.SysDictTypeMapper;
import com.lcb.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {

    private final SysDictDataMapper dictDataMapper;

    public SysDictTypeServiceImpl(SysDictDataMapper dictDataMapper) {
        this.dictDataMapper = dictDataMapper;
    }

    @Override
    public List<SysDictData> selectDataByType(String dictType) {
        return dictDataMapper.selectByType(dictType);
    }
}
