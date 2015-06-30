package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Registers a ColumnValueValidator with DBFlow to be applied to
 * a specific column from a specific table.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ColumnValueValidator {

    /**
     * If set (default), if an invalid value is passed, we will throw an exception.
     */
    int HANDLING_CRASH = -1;

    /**
     * If set, the default value for a column is used.
     */
    int HANDLING_USE_DEFAULT = 0;

    /**
     * If set, you can specify a replacement string to be used in a more graceful fallback.
     */
    int HANDLING_SPECIFY_STRING = 1;

    /**
     * @return {@link #HANDLING_CRASH} by default. Specify how the column value falls back.
     */
    int handling() default HANDLING_CRASH;

    /**
     * @return The value you wish to use to fallback in the case the {@link #handling()} is {@link #HANDLING_SPECIFY_STRING}
     * and the ColumnValueValidator fails.
     */
    String stringFallback() default "";
}
