package com.raizlabs.android.dbflow.structure;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    public enum ConflictAction {
        ROLLBACK, ABORT, FAIL, IGNORE, REPLACE
    }

    String name() default "";

    int length() default -1;

    boolean notNull() default false;

    ConflictAction onNullConflict() default ConflictAction.FAIL;

    ColumnType columnType() default @ColumnType;

    boolean unique() default false;

    ConflictAction onUniqueConflict() default ConflictAction.FAIL;

    String foreignColumn() default "";
}
