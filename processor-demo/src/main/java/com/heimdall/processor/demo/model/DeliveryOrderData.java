package com.heimdall.processor.demo.model;

import com.heimdall.processor.core.model.MappingModel;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author crh
 * @since 2020/10/12
 */
@Data
@MappingModel(suffixName = "DTO", excludeFields = {"createdBy"}, addFieldClasses = {DeliveryOrderField.class})
@MappingModel(suffixName = "VO", packageName = "com.heimdall.processor.demo.model.vo", includeFields = {"id", "createdAt","deliveryOrderProducts", "deliveryOrderItems"})
public class DeliveryOrderData {

    private Long id;

    private Date createdAt;

    private Long createdBy;

    private List<DeliveryOrderProductData> deliveryOrderProducts;

    private DeliveryOrderStatusEnum status;

    private List<DeliveryOrderItemData> deliveryOrderItems;

}
