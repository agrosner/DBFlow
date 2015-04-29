package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: Marks a field as corresponding to a column in the DB.
 * When adding new columns or changing names, you need to define a new {@link com.raizlabs.android.dbflow.annotation.Migration}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Column {

    /**
     * @return The name of the column. The default is the field name.
     */
    String name() default "";

    /**
     * @return An optional column length
     */
    int length() default -1;

    /**
     * Marks this field as not null and will throw an exception if it is.
     *
     * @return if field cannot be null.
     */
    boolean notNull() default false;

    /**
     * Defines how to handle conflicts for not null column
     *
     * @return a {@link com.raizlabs.android.dbflow.annotation.ConflictAction} enum
     */
    ConflictAction onNullConflict() default ConflictAction.FAIL;

    /**
     * @return Marks the field as having a specified collation to use in it's creation.
     */
    String collate() default "";

    /**
     * @return Adds a default value for this column when saving
     */
    String defaultValue() default "";

    /**
     * Defines how to handle conflicts for a unique column
     *
     * @return a {@link com.raizlabs.android.dbflow.annotation.ConflictAction} enum
     */
    ConflictAction onUniqueConflict() default ConflictAction.FAIL;

    /**
     * Defines {@link ForeignKeyAction} action to be performed
     * on delete of referenced record. Defaults to {@link ForeignKeyAction#NO_ACTION}. Used only when
     * columnType is {@link ForeignKey}.
     *
     * @return {@link ForeignKeyAction}
     */
    ForeignKeyAction onDelete() default ForeignKeyAction.NO_ACTION;

    /**
     * Defines {@link ForeignKeyAction} action to be performed
     * on update of referenced record. Defaults to {@link ForeignKeyAction#NO_ACTION}. Used only when
     * columnType is {@link ForeignKey}.
     *
     * @return {@link ForeignKeyAction}
     */
    ForeignKeyAction onUpdate() default ForeignKeyAction.NO_ACTION;
}
