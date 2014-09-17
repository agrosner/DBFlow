package com.grosner.dbflow.structure;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public @interface ColumnType {

    public static final int NORMAL = -1;

    public static final int PRIMARY_KEY = 0;

    public static final int PRIMARY_KEY_AUTO_INCREMENT = 1;

    public static final int FOREIGN_KEY = 2;

    int value() default NORMAL;
}
