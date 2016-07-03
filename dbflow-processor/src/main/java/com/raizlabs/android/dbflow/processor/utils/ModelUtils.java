package com.raizlabs.android.dbflow.processor.utils;

import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ManyToMany;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

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

    public static String getVariable(boolean isModelContainer) {
        return isModelContainer ? "modelContainer" : "model";
    }

    public static ParameterizedTypeName getModelContainerType(ProcessorManager manager, TypeName modelType) {
        return ParameterizedTypeName.get(ClassNames.MODEL_CONTAINER,
                modelType, TypeName.get(manager.getTypeUtils().getWildcardType(null, null)));
    }

    public static String getWrapper() {
        return "wrapper";
    }
}
