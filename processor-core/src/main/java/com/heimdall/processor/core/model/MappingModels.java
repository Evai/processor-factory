package com.heimdall.processor.core.model;

import java.lang.annotation.*;

/**
 * @author crh
 * @since 2020-10-03
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Documented
public @interface MappingModels {

   MappingModel[] value();

}
