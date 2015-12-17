package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Allows you to specify a non-Column to be inherited and used as a {@link PrimaryKey}
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface InheritedPrimaryKey {

    /**
     * @return The primary key annotation as if it was part of the class
     */
    PrimaryKey primaryKey();

    /**
     * @return The column annotation as if it was part of the class
     */
    Column column();

    /**
     * @return The field name that an inherited column uses. It must match exactly case-by-case to the field you're referencing.
     * If the field is private, the {@link PrimaryKey} allows you to define getter and setters for it.
     */
    String fieldName();
}
