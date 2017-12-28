package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Allows a {@link ColumnMap} to specify a reference override for its fields. Anything not
 * defined here will not be used.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface ColumnMapReference {

    /**
     * @return The local column name that will be referenced in the DB
     */
    String columnName();

    /**
     * @return The column name in the referenced table
     */
    String columnMapFieldName();

    /**
     * @return Specify the {@link NotNull} annotation here and it will get pasted into the reference definition.
     */
    NotNull notNull() default @NotNull(onNullConflict = ConflictAction.NONE);
}
