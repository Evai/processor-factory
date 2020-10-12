package com.heimdall.processor.demo.model;

import com.heimdall.processor.core.model.MappingModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author crh
 * @since 2020/10/12
 */
@Data
@Accessors(chain = true)
@MappingModel(suffixName = "DTO", excludeFields = "canceledQuantity")
@MappingModel(suffixName = "VO",packageName = "com.heimdall.processor.demo.model.vo")
public class DeliveryOrderProductData {

    private Long id;

    private Long productId;

    private String productName;

    private Integer quantity;

    private Integer canceledQuantity;


}