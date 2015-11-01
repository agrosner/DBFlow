package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface IndexGroup {

    int GENERIC = -1;

    /**
     * @return The number that each contained {@link Index} points to, so they can be combined into a single index.
     * If {@link #GENERIC}, this will assume a generic index that covers the whole table.
     */
    int number() default GENERIC;

    /**
     * @return The name of this index. It must be unique from other {@link IndexGroup}.
     */
    String name();

    /**
     * @return If true, this will disallow duplicate values to be inserted into the table.
     */
    boolean unique() default false;
}
