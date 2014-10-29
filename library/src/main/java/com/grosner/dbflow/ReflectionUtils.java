package com.grosner.dbflow;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.structure.BaseModelView;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelAdapter;
import com.grosner.dbflow.structure.ModelViewDefinition;
import com.grosner.dbflow.structure.container.ContainerAdapter;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides some handy reflection methods.
 */
public class ReflectionUtils {

    /**
     * Gets all of the {@link com.grosner.dbflow.annotation.Column} fields
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
        return type.getSuperclass() != null && (type.getSuperclass().equals(superClass) || isSubclassOf(type.getSuperclass(), superClass));

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
     * Returns whether the passed in class implements {@link com.grosner.dbflow.structure.BaseModelView}
     *
     * @param discoveredClass
     * @return true if the class can be assigned to ModelView
     */
    public static boolean implementsModelView(Class discoveredClass) {
        return BaseModelView.class.isAssignableFrom(discoveredClass);
    }

    /**
     * Returns whether the passed in class implements {@link com.grosner.dbflow.structure.ModelViewDefinition}
     *
     * @param discoveredClass
     * @return true if the class can be assigned to ModelView
     */
    public static boolean implementsModelViewDefinition(Class discoveredClass) {
        return ModelViewDefinition.class.isAssignableFrom(discoveredClass);
    }

    public static boolean implementsModelAdapter(Class discoveredClass) {
        return ModelAdapter.class.isAssignableFrom(discoveredClass);
    }

    public static boolean implementsContainerAdapter(Class discoveredClass) {
        return ContainerAdapter.class.isAssignableFrom(discoveredClass);
    }
}
