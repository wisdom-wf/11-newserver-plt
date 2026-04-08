package com.elderlycare.common;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long total;
    private Integer page;
    private Integer pageSize;
    private Integer pages;
    private List<T> list;

    public PageResult() {
    }

    public PageResult(Long total, Integer page, Integer pageSize, List<T> list) {
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
        this.list = list;
    }

    public static <T> PageResult<T> of(Long total, Integer page, Integer pageSize, List<T> list) {
        return new PageResult<>(total, page, pageSize, list);
    }
}
