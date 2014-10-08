package com.grosner.dbflow;

import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.runtime.observer.ModelObserver;
import com.grosner.dbflow.structure.Column;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelView;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides some handy reflection methods.
 */
public class ReflectionUtils {

    /**
     * Gets all of the {@link com.grosner.dbflow.structure.Column} fields
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
     * Returns whether the passed in class implements {@link com.grosner.dbflow.structure.Model}
     *
     * @param clazz
     * @return true if it implements model, rather can be assigned to the Model class
     */
    public static boolean implementsModel(Class clazz) {
        return Model.class.isAssignableFrom(clazz);
    }

    /**
     * Returns if the type is a subclass of another. Runs up the class inheritance hiearchy until its a base class or the superclass is the same.
     *
     * @param type       The class we want to check
     * @param superClass The superclass we want to see that this class is a subclass of
     * @return
     */
    public static boolean isSubclassOf(Class type, Class superClass) {
        if (type.getSuperclass() != null) {
            return type.getSuperclass().equals(superClass) || isSubclassOf(type.getSuperclass(), superClass);
        }

        return false;
    }

    /**
     * Returns whether the passed in class implements {@link com.grosner.dbflow.converter.TypeConverter}
     *
     * @param discoveredClass
     * @return true if class can be assigned to TypeConverter
     */
    public static boolean implementsTypeConverter(Class discoveredClass) {
        return TypeConverter.class.isAssignableFrom(discoveredClass);
    }

    /**
     * Returns whether the passed in class implements {@link com.grosner.dbflow.structure.ModelView}
     *
     * @param discoveredClass
     * @return true if the class can be assigned to ModelView
     */
    public static boolean implementsModelView(Class discoveredClass) {
        return ModelView.class.isAssignableFrom(discoveredClass);
    }

    /**
     * Returns whether the passed in class implements {@link com.grosner.dbflow.runtime.observer.ModelObserver}
     *
     * @param discoveredClass
     * @return true if the class can be assigned to {@link com.grosner.dbflow.runtime.observer.ModelObserver}
     */
    public static boolean implementsModelObserver(Class<?> discoveredClass) {
        return ModelObserver.class.isAssignableFrom(discoveredClass);
    }

}
