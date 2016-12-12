package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Specifies that a {@link Column} is not null.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface NotNull {

    /**
     * Defines how to handle conflicts for not null column
     *
     * @return a {@link com.raizlabs.android.dbflow.annotation.ConflictAction} enum
     */
    ConflictAction onNullConflict() default ConflictAction.FAIL;

}
