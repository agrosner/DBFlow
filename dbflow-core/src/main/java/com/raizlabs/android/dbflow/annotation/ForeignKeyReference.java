package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Description: Used inside of {@link ForeignKey#references()}, describes the
 * local column name, type, and referencing table column name.
 * <p></p>
 * Note: the type of the local column must match the
 * column type of the referenced column. By using a field as a Model object,
 * you will need to ensure the same types are used.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface ForeignKeyReference {

    /**
     * @return The local column name that will be referenced in the DB
     */
    String columnName();

    /**
     * @return The column name in the referenced table
     */
    String foreignKeyColumnName();

    /**
     * @return Specify the {@link NotNull} annotation here and it will get pasted into the reference definition.
     */
    NotNull notNull() default @NotNull(onNullConflict = ConflictAction.NONE);
}
