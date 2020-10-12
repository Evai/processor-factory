package com.heimdall.processor.core.constant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author crh
 * @since 2020-10-03
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface MappingConstant {

    /**
     * mapping class name
     *
     * @return String
     */
    String className() default "";

    /**
     * mapping with enum name
     *
     * @return boolean
     */
    boolean withEnumName() default false;

}
