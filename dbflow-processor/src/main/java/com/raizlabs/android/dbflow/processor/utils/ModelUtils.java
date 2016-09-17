package com.raizlabs.android.dbflow.processor.utils;

import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ManyToMany;

import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

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

    public static TypeMirror getReferencedClassFromAnnotation(ManyToMany annotation) {
        TypeMirror clazz = null;
        if (annotation != null) {
            try {
                annotation.referencedTable();
            } catch (MirroredTypeException mte) {
                clazz = mte.getTypeMirror();
            }
        }
        return clazz;
    }

    public static String getVariable() {
        return "model";
    }

    public static String getWrapper() {
        return "wrapper";
    }
}
