package com.grosner.dbflow.structure;

import android.support.annotation.IntDef;

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
     * Default value, it is just an ordinary column
     */
    public static final int NORMAL = -1;

    /**
     * The field is marked as a primary key and cannot be repeated.
     */
    public static final int PRIMARY_KEY = 0;

    /**
     * The field is marked as an auto-incrementing primary key and will increment with every new row.
     */
    public static final int PRIMARY_KEY_AUTO_INCREMENT = 1;

    /**
     * The field references another column from another table and will retrieve the object upon load of the {@link com.grosner.dbflow.structure.Model}
     */
    public static final int FOREIGN_KEY = 2;

    @ColumnType int columnType() default NORMAL;

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
     *
     * @return
     */
    ForeignKeyReference[] references() default {};

    /**
     * This is how to resolve null or unique conflicts with a field marked as {@link #notNull()}
     * or {@link #unique()}
     */
    public enum ConflictAction {
        ROLLBACK, ABORT, FAIL, IGNORE, REPLACE
    }

    /**
     * Lets you specify if its a primary, foreign key, or autoincrementing field
     *
     * @return
     */
    @IntDef({NORMAL, PRIMARY_KEY, PRIMARY_KEY_AUTO_INCREMENT, FOREIGN_KEY})
    @interface ColumnType {
    }
}
