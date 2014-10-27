package com.grosner.dbflow.structure;

import java.lang.reflect.Field;

/**
 * Author: andrewgrosner
 * Description: Provides some basic database structure utility methods.
 */
public class StructureUtils {

    /**
     * Checks to see if field is a {@link com.grosner.dbflow.structure.Column#PRIMARY_KEY}
     *
     * @param field
     * @return
     */
    public static boolean isPrimaryKey(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isPrimary = column != null;
        if (isPrimary) {
            isPrimary = (column.columnType() != Column.FOREIGN_KEY && column.columnType() != Column.NORMAL);
        }
        return isPrimary;
    }

    /**
     * Checks to see if field is a {@link com.grosner.dbflow.structure.Column#FOREIGN_KEY}
     *
     * @param field
     * @return
     */
    public static boolean isForeignKey(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isForeign = column != null;
        if (isForeign) {
            isForeign = column.columnType() == Column.FOREIGN_KEY;
        }
        return isForeign;
    }

    /**
     * Checks to see if field is not {@link com.grosner.dbflow.structure.Column#PRIMARY_KEY_AUTO_INCREMENT}
     *
     * @param field
     * @return
     */
    public static boolean isPrimaryKeyNoIncrement(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isPrimary = column != null;
        if (isPrimary) {
            isPrimary = column.columnType() == Column.PRIMARY_KEY;
        }
        return isPrimary;
    }

    /**
     * Returns true if the field is {@link com.grosner.dbflow.structure.Column#PRIMARY_KEY_AUTO_INCREMENT}
     *
     * @param field
     * @return
     */
    public static boolean isPrimaryKeyAutoIncrement(Field field) {
        Column column = field.getAnnotation(Column.class);
        boolean isPrimary = column != null;
        if (isPrimary) {
            isPrimary = column.columnType() == Column.PRIMARY_KEY_AUTO_INCREMENT;
        }
        return isPrimary;
    }


}
