package com.heimdall.processor.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Project:               spring-wild
 * Author:                crh
 * Company:               Big Player Group
 * Created Date:          2020/9/23
 * Description:   {类描述}
 * Copyright @ 2017-2020 BIGPLAYER.GROUP – Confidential and Proprietary
 * <p>
 * History:
 * ------------------------------------------------------------------------------
 * Date            |time        |Author    |Change Description
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
