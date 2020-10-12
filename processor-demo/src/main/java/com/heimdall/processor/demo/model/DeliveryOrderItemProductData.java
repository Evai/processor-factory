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
@MappingModel(suffixName = "DTO")
@MappingModel(suffixName = "Resp",packageName = "com.heimdall.processor.demo.model.resp")
public class DeliveryOrderItemProductData {

    private Long id;

    private String deliveryOrderProductName;

    private Integer quantity;


}