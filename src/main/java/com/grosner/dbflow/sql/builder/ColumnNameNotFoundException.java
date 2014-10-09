package com.grosner.dbflow.sql.builder;

import com.grosner.dbflow.structure.TableStructure;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ColumnNameNotFoundException extends RuntimeException {
    public ColumnNameNotFoundException(String columnName, TableStructure tableStructure) {
        super("The column : " + columnName + " was not found for " + tableStructure.getTableName());
    }
}
