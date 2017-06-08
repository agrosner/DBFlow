package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: An annotation used to ignore a column in the {@link Table#allFields()} instance.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface ColumnIgnore {
}
