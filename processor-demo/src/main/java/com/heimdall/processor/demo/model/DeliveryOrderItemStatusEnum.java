package com.heimdall.processor.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author crh
 * @since 2020/10/12
 */
@AllArgsConstructor
@Getter
public enum DeliveryOrderItemStatusEnum {
    /**
     * 发货单状态
     */
    DELIVERING(0, "发货中"),
    DELIVERED(1, "已发货"),
    CLOSED(-1, "已关闭"),
    ;

    private final Integer code;
    private final String desc;
}
