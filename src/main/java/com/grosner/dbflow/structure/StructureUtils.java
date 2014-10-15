package com.grosner.dbflow.structure;

import java.lang.reflect.Field;

/**
 * Author: andrewgrosner
 * Description: Provides some basic database structure utility methods.
 */
public class StructureUtils {

    /**
     * Checks to see if field is a {@link com.grosner.dbflow.structure.ColumnType#PRIMARY_KEY}
     *
     * @param field
     * @return
     */
    public static boolean isPrimaryKey(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isPrimary = column != null;
        if (isPrimary) {
            isPrimary = (column.value().value() != ColumnType.FOREIGN_KEY && column.value().value() != ColumnType.NORMAL);
        }
        return isPrimary;
    }

    /**
     * Checks to see if field is a {@link com.grosner.dbflow.structure.ColumnType#FOREIGN_KEY}
     *
     * @param field
     * @return
     */
    public static boolean isForeignKey(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isForeign = column != null;
        if (isForeign) {
            isForeign = column.value().value() == ColumnType.FOREIGN_KEY;
        }
        return isForeign;
    }

    /**
     * Checks to see if field is not {@link com.grosner.dbflow.structure.ColumnType#PRIMARY_KEY_AUTO_INCREMENT}
     *
     * @param field
     * @return
     */
    public static boolean isPrimaryKeyNoIncrement(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isPrimary = column != null;
        if (isPrimary) {
            isPrimary = column.value().value() == ColumnType.PRIMARY_KEY;
        }
        return isPrimary;
    }

    /**
     * Returns true if the field is {@link com.grosner.dbflow.structure.ColumnType#PRIMARY_KEY_AUTO_INCREMENT}
     *
     * @param field
     * @return
     */
    public static boolean isPrimaryKeyAutoIncrement(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isPrimary = column != null;
        if (isPrimary) {
            isPrimary = column.value().value() == ColumnType.PRIMARY_KEY_AUTO_INCREMENT;
        }
        return isPrimary;
    }


}
