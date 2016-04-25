package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Add this to a {@link Column}
 * field to specify the key for a ModelContainer object that the column references. It
 * enables a different key than column name.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface ContainerKey {

    /**
     * @return The key that we use to retrieve the value from the ModelContainer. If null or empty,
     * it defaults to the column name.
     */
    String value() default "";

    /**
     * @return True if you want to place a default value for the column when loading from the Cursor. Only
     * valid for {@link Column} annotated fields.
     */
    boolean putDefault() default true;
}
