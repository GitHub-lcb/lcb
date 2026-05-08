package com.lcb.common.core;

import lombok.Data;

@Data
public class PageQuery {
    private int page = 1;
    private int pageSize = 10;
}
