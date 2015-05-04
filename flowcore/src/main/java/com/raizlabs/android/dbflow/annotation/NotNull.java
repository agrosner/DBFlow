package com.raizlabs.android.dbflow.annotation;

/**
 * Description: Specifies that a {@link Column} is not null.
 */
public @interface NotNull {

    /**
     * Defines how to handle conflicts for not null column
     *
     * @return a {@link com.raizlabs.android.dbflow.annotation.ConflictAction} enum
     */
    ConflictAction onNullConflict() default ConflictAction.FAIL;

}
