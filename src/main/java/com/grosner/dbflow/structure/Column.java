package com.grosner.dbflow.structure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: The main annotation that marks a field as
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    /**
     * Lets you specify the {@link com.grosner.dbflow.structure.ColumnType}
     *
     * @return
     */
    ColumnType value() default @ColumnType;

    /**
     * This is how to resolve null or unique conflicts with a field marked as {@link #notNull()}
     * or {@link #unique()}
     */
    public enum ConflictAction {
        ROLLBACK, ABORT, FAIL, IGNORE, REPLACE
    }

    /**
     * The name of the column. The default is the field name.
     *
     * @return
     */
    String name() default "";

    /**
     * Specify an optional column length
     *
     * @return
     */
    int length() default -1;

    /**
     * Marks this field as not null and will throw an exception if it is.
     *
     * @return
     */
    boolean notNull() default false;

    /**
     * Defines how to handle conflicts for not null column
     *
     * @return
     */
    ConflictAction onNullConflict() default ConflictAction.FAIL;

    /**
     * Marks the field as unique, meaning its value cannot be repeated. It is, however,
     * NOT a primary key.
     *
     * @return
     */
    boolean unique() default false;

    /**
     * Defines how to handle conflicts for a unique column
     *
     * @return
     */
    ConflictAction onUniqueConflict() default ConflictAction.FAIL;

    /**
     * Defines the references to the foreignkeys
     * @return
     */
    ForeignKeyReference[] references() default {};
}
