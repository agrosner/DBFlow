package com.raizlabs.android.dbflow;

import com.raizlabs.android.dbflow.structure.Column;
import com.raizlabs.android.dbflow.structure.Model;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ReflectionUtils {

    public static List<Field> getAllFields(List<Field> outFields, Class<?> inClass) {
        for (Field field : inClass.getDeclaredFields()) {
            if(field.isAnnotationPresent(Column.class)) {
                outFields.add(field);
            }
        }
        if (inClass.getSuperclass() != null && !inClass.getSuperclass().equals(Model.class)) {
            outFields = getAllFields(outFields, inClass.getSuperclass());
        }
        return outFields;
    }

    /**
     * Returns whether the passed in class implements {@link com.raizlabs.android.dbflow.structure.Model}
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
}
