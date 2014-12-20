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
     * Default value, it is just an ordinary column
     */
    public static final int NORMAL = -1;

    /**
     * The field is marked as a primary key and cannot be repeated.
     */
    public static final int PRIMARY_KEY = 0;

    /**
     * The field is marked as an auto-incrementing primary key and will increment with every new row. Only
     * one column should be auto-incrementing.
     */
    public static final int PRIMARY_KEY_AUTO_INCREMENT = 1;

    /**
     * The field references another column from another table and will retrieve the object upon load of the Model
     */
    public static final int FOREIGN_KEY = 2;

    /**
     * Specifies the column type. Can be {@link #NORMAL}, {@link #PRIMARY_KEY}, {@link #PRIMARY_KEY_AUTO_INCREMENT},
     * or {@link #FOREIGN_KEY}
     *
     * @return the columnType int
     */
    int columnType() default NORMAL;

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
     * Marks the field as unique, meaning its value cannot be repeated. It is, however,
     * NOT a primary key.
     *
     * @return if field is unique
     */
    boolean unique() default false;

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
     * Defines the references for a composite {@link #FOREIGN_KEY} definition. It enables for multiple local
     * columns that reference another Model's primary keys.
     *
     * @return the set of references
     */
    ForeignKeyReference[] references() default {};

}
