package com.lcb.common.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageUtils {

    public static <E, V> Page<V> convert(IPage<E> entityPage, Function<E, V> converter) {
        Page<V> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
            .map(converter)
            .collect(Collectors.toList()));
        return voPage;
    }
}
