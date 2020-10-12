package com.heimdall.processor.core.model;

import java.lang.annotation.*;

/**
 * @author crh
 * @since 2020-10-03
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(MappingModels.class)
public @interface MappingModel {

    String suffixName();

    String packageName() default "";

    String[] includeFields() default {};

    String[] excludeFields() default {};

    Class<?>[] addFieldClasses() default {};

}
