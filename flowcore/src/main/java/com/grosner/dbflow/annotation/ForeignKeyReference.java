package com.grosner.dbflow.annotation;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public @interface ForeignKeyReference {

    String columnName();

    /**
     * Needs to match both tables!
     *
     * @return
     */
    Class<?> columnType();

    String foreignColumnName();
}
