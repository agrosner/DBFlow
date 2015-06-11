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
public @interface InheritedColumn {

    /**
     * @return The column annotation as if it was part of the class
     */
    Column column();

    /**
     * @return The field name that an inherited column uses. It must match exactly case-by-case to the field you're referencing.
     * If the field is private, the {@link Column} allows you to define getter and setters for it.
     */
    String fieldName();

}
