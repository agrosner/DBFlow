package com.grosner.dbflow.structure;

import com.grosner.dbflow.ReflectionUtils;
import com.grosner.dbflow.StringUtils;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.runtime.observer.ModelObserver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Author: andrewgrosner
 * Contributors: { }
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
