package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.*
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.DefinitionUtils
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.raizlabs.android.dbflow.processor.utils.`override fun`
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier

/**
 * Description: Assists in writing methods for adapters
 */
object InternalAdapterHelper {

    fun writeGetModelClass(typeBuilder: TypeSpec.Builder, modelClassName: ClassName?) = typeBuilder.apply {
        `override fun`(ParameterizedTypeName.get(ClassName.get(Class::class.java), modelClassName), "getModelClass") {
            modifiers(public, final)
            `return`("\$T.class", modelClassName)
        }
    }

    fun writeGetTableName(typeBuilder: TypeSpec.Builder, tableName: String?) = typeBuilder.apply {
        `override fun`(String::class, "getTableName") {
            modifiers(public, final)
            `return`(QueryBuilder.quote(tableName).S)
        }
    }

    fun writeUpdateAutoIncrement(typeBuilder: TypeSpec.Builder, modelClassName: TypeName?,
                                 autoIncrementDefinition: ColumnDefinition) = typeBuilder.apply {
        `override fun`(TypeName.VOID, "updateAutoIncrement", param(modelClassName!!, ModelUtils.variable),
                param(Number::class, "id")) {
            modifiers(public, final)
            addCode(autoIncrementDefinition.updateAutoIncrementMethod)
        }
    }

    fun writeGetCachingId(typeBuilder: TypeSpec.Builder, modelClassName: TypeName?,
                          primaryColumns: List<ColumnDefinition>) {
        if (primaryColumns.size > 1) {
            var methodBuilder: MethodSpec.Builder = MethodSpec.methodBuilder("getCachingColumnValuesFromModel")
                    .addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ArrayTypeName.of(Any::class.java), "inValues")
                    .addParameter(modelClassName, ModelUtils.variable)
            for (i in primaryColumns.indices) {
                val column = primaryColumns[i]
                methodBuilder.addCode(column.getColumnAccessString(i))
            }
            methodBuilder.addStatement("return \$L", "inValues").returns(ArrayTypeName.of(Any::class.java))
            typeBuilder.addMethod(methodBuilder.build())

            methodBuilder = MethodSpec.methodBuilder("getCachingColumnValuesFromCursor")
                    .addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ArrayTypeName.of(Any::class.java), "inValues")
                    .addParameter(ClassNames.CURSOR, "cursor")
            for (i in primaryColumns.indices) {
                val column = primaryColumns[i]
                val method = DefinitionUtils.getLoadFromCursorMethodString(column.elementTypeName, column.wrapperTypeName)
                methodBuilder.addStatement("inValues[\$L] = \$L.\$L(\$L.getColumnIndex(\$S))", i, LoadFromCursorMethod.PARAM_CURSOR,
                        method, LoadFromCursorMethod.PARAM_CURSOR, column.columnName)
            }
            methodBuilder.addStatement("return \$L", "inValues").returns(ArrayTypeName.of(Any::class.java))
            typeBuilder.addMethod(methodBuilder.build())
        } else {
            // single primary key
            var methodBuilder: MethodSpec.Builder = MethodSpec.methodBuilder("getCachingColumnValueFromModel")
                    .addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(modelClassName, ModelUtils.variable)
            methodBuilder.addCode(primaryColumns[0].getSimpleAccessString())
                    .returns(Any::class.java)
            typeBuilder.addMethod(methodBuilder.build())

            methodBuilder = MethodSpec.methodBuilder("getCachingColumnValueFromCursor")
                    .addAnnotation(Override::class.java).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ClassNames.CURSOR, "cursor")
            val column = primaryColumns[0]
            val method = DefinitionUtils.getLoadFromCursorMethodString(column.elementTypeName, column.wrapperTypeName)
            methodBuilder.addStatement("return \$L.\$L(\$L.getColumnIndex(\$S))", LoadFromCursorMethod.PARAM_CURSOR,
                    method, LoadFromCursorMethod.PARAM_CURSOR, column.columnName).returns(Any::class.java)
            typeBuilder.addMethod(methodBuilder.build())

            methodBuilder = MethodSpec.methodBuilder("getCachingId")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(modelClassName, ModelUtils.variable)
                    .addStatement("return getCachingColumnValueFromModel(\$L)",
                            ModelUtils.variable).returns(TypeName.OBJECT)
            typeBuilder.addMethod(methodBuilder.build())
        }
    }

}
