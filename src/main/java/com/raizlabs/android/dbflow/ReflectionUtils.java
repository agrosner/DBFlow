package com.raizlabs.android.dbflow;

import com.raizlabs.android.dbflow.converter.ForeignKeyConverter;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.structure.Column;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.StructureUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ReflectionUtils {

    /**
     * Gets all of the {@link com.raizlabs.android.dbflow.structure.Column} fields
     *
     * @param outFields
     * @param inClass
     * @return
     */
    public static List<Field> getAllColumns(List<Field> outFields, Class<?> inClass) {
        for (Field field : inClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                outFields.add(field);
            }
        }
        if (inClass.getSuperclass() != null && !inClass.getSuperclass().equals(Model.class)) {
            outFields = getAllColumns(outFields, inClass.getSuperclass());
        }
        return outFields;
    }

    /**
     * Gets all of the primary fields from the specified class.
     *
     * @param outFields
     * @param inClass
     * @return
     */
    public static List<Field> getPrimaryColumnFields(List<Field> outFields, Class<?> inClass) {
        for (Field field : inClass.getDeclaredFields()) {
            if (StructureUtils.isPrimaryKey(field)) {
                outFields.add(field);
            }
        }
        if (inClass.getSuperclass() != null && !inClass.getSuperclass().equals(Model.class)) {
            outFields = getAllColumns(outFields, inClass.getSuperclass());
        }
        return outFields;
    }

    /**
     * Returns whether the passed in class implements {@link com.raizlabs.android.dbflow.structure.Model}
     *
     * @param clazz
     * @return true if it implements model, rather can be assigned to the Model class
     */
    public static boolean implementsModel(Class clazz) {
        return Model.class.isAssignableFrom(clazz);
    }

    public static boolean isSubclassOf(Class type, Class superClass) {
        if (type.getSuperclass() != null) {
            if (type.getSuperclass().equals(superClass)) {
                return true;
            }

            return isSubclassOf(type.getSuperclass(), superClass);
        }

        return false;
    }

    /**
     * Returns whether the passed in class implements {@link com.raizlabs.android.dbflow.converter.TypeConverter}
     *
     * @param discoveredClass
     * @return true if class can be assigned to TypeConverter
     */
    public static boolean implementsTypeConverter(Class discoveredClass) {
        return TypeConverter.class.isAssignableFrom(discoveredClass);
    }

    /**
     * Returns whether the passed in class implements {@link com.raizlabs.android.dbflow.converter.ForeignKeyConverter}
     *
     * @param discoveredClass
     * @return true if class can be assigned to ForeignKeyConverter
     */
    public static boolean implementsForeignKeyConverter(Class discoveredClass) {
        return ForeignKeyConverter.class.isAssignableFrom(discoveredClass);
    }
}
