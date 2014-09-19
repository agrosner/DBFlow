package com.grosner.dbflow.structure;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Defines the type of column.
 */
public @interface ColumnType {

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

    /**
     * Returns the type of the column in {@link com.grosner.dbflow.structure.Column}
     *
     * @return
     */
    int value() default NORMAL;
}
