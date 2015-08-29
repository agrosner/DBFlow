package com.raizlabs.android.dbflow.processor.definition;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.lang.model.element.Modifier;

/**
 * Description: Assists in writing methods for adapters
 */
public class InternalAdapterHelper {

    public static void writeGetModelClass(TypeSpec.Builder typeBuilder, final ClassName modelClassName) {
        typeBuilder.addMethod(MethodSpec.methodBuilder("getModelClass")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return $T.class", modelClassName)
                .returns(ParameterizedTypeName.get(ClassName.get(Class.class), modelClassName))
                .build());
    }

    public static void writeGetTableName(TypeSpec.Builder typeBuilder, final String tableName) {
        typeBuilder.addMethod(MethodSpec.methodBuilder("getTableName")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return `$L`", tableName)
                .returns(ClassName.get(String.class))
                .build());
    }
}
