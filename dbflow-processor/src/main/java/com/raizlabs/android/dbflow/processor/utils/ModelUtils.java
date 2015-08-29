package com.raizlabs.android.dbflow.processor.utils;

import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;

import javax.lang.model.type.MirroredTypeException;

/**
 * Author: andrewgrosner
 * Description:
 */
public class ModelUtils {

    public static String getClassFromAnnotation(ForeignKeyReference annotation) {
        String clazz = null;
        if (annotation != null) {
            try {
                annotation.columnType();
            } catch (MirroredTypeException mte) {
                clazz = mte.getTypeMirror().toString();
            }
        }
        return clazz;
    }

    public static String getVariable(boolean isModelContainer) {
        return isModelContainer ? "modelContainer" : "model";
    }
}
