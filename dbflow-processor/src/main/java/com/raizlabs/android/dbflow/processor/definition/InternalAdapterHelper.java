package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

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
                .addStatement("return $S", QueryBuilder.quote(tableName))
                .returns(ClassName.get(String.class))
                .build());
    }

    public static void writeUpdateAutoIncrement(TypeSpec.Builder typeBuilder, final TypeName modelClassName,
                                                ColumnDefinition autoIncrementDefinition, boolean isModelContainer) {
        typeBuilder.addMethod(MethodSpec.methodBuilder("updateAutoIncrement")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(modelClassName, ModelUtils.getVariable(isModelContainer))
                .addParameter(ClassName.get(Number.class), "id")
                .addCode(autoIncrementDefinition.getUpdateAutoIncrementMethod(isModelContainer)).build());
    }

    public static void writeGetCachingId(TypeSpec.Builder typeBuilder, final TypeName modelClassName,
                                         ColumnDefinition cachingDefinition, boolean isModelContainer) {
        typeBuilder.addMethod(MethodSpec.methodBuilder("getCachingId")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(modelClassName, ModelUtils.getVariable(isModelContainer))
                .addStatement("return $L", cachingDefinition.getColumnAccessString(isModelContainer, false))
                .returns(cachingDefinition.elementTypeName.box()).build());
    }

}
