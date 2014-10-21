package com.grosner.dbflow.structure;

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
