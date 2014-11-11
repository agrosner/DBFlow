package com.grosner.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: Marks a field as corresponding to a column in the DB.
 * When adding new columns or changing names, you need to define a new {@link com.grosner.dbflow.annotation.Migration}.
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
     * @return
     */
    int columnType() default NORMAL;

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
     * Defines the references for a composite {@link #FOREIGN_KEY} definition. It enables for multiple local
     * columns that reference another Model's primary keys.
     *
     * @return
     */
    ForeignKeyReference[] references() default {};

}
