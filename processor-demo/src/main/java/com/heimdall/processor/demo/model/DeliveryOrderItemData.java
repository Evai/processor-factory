package com.heimdall.processor.demo.model;


import com.heimdall.processor.core.model.MappingModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * @author crh
 * @since 2020/10/12
 */
@Data
@Accessors(chain = true)
@MappingModel(suffixName = "DTO")
@MappingModel(suffixName = "VO",packageName = "com.heimdall.processor.demo.model.vo")
public class DeliveryOrderItemData {

    private Long id;

    private Date createdAt;

    private Date updatedAt;

    private Long createdBy;

    private Long updatedBy;

    private DeliveryOrderItemStatusEnum status;

    private List<DeliveryOrderItemProductData> deliveryOrderItemProducts;

}
