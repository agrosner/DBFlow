package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Marks the field as unique, meaning its value cannot be repeated. It is, however,
 * NOT a primary key.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Unique {

    /**
     * @return if field is unique. If false, we expect {@link #uniqueGroups()} to be specified.`
     */
    boolean unique() default true;

    /**
     * @return Marks a unique field as part of a unique group. For every unique number entered,
     * it will generate a UNIQUE() column statement.
     */
    int[] uniqueGroups() default {};
}
