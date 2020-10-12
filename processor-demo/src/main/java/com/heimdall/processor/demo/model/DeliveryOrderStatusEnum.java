package com.heimdall.processor.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author crh
 * @since 2020/10/12
 */
@AllArgsConstructor
@Getter
public enum DeliveryOrderStatusEnum {
    /**
     * 发货订单状态
     */
    TO_DELIVERY(0, "待发货"),
    DELIVERING(1, "发货中"),
    DELIVERED(2, "已发货"),
    CLOSED(-1, "已关闭"),
    ;

    private final Integer code;
    private final String desc;
}
