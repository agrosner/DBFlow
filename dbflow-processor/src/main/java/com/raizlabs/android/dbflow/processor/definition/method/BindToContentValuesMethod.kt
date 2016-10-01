package com.raizlabs.android.dbflow.processor.definition.method

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition
import com.raizlabs.android.dbflow.processor.utils.ModelUtils
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Modifier

/**
 * Description: Writes the bind to content values method in the ModelDAO.
 */
class BindToContentValuesMethod(private val baseTableDefinition: BaseTableDefinition,
                                private val isInsert: Boolean,
                                private val isModelContainerAdapter: Boolean,
                                private val implementsContentValuesListener: Boolean) : MethodDefinition {

    override val methodSpec: MethodSpec
        get() {
            val methodBuilder = MethodSpec.methodBuilder(if (isInsert) "bindToInsertValues" else "bindToContentValues")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(ClassNames.CONTENT_VALUES, PARAM_CONTENT_VALUES)
                    .addParameter(baseTableDefinition.parameterClassName, ModelUtils.variable)
                    .returns(TypeName.VOID)

            if (isInsert) {
                baseTableDefinition.columnDefinitions.forEach {
                    if (!it.isPrimaryKeyAutoIncrement && !it.isRowId) {
                        methodBuilder.addCode(it.getContentValuesStatement(isModelContainerAdapter))
                    }
                }

                if (implementsContentValuesListener) {
                    methodBuilder.addStatement("\$L.onBindTo\$LValues(\$L)",
                            ModelUtils.variable, if (isInsert) "Insert" else "Content", PARAM_CONTENT_VALUES)
                }
            } else {
                if (baseTableDefinition.hasAutoIncrement || baseTableDefinition.hasRowID) {
                    val autoIncrement = baseTableDefinition.autoIncrementColumn
                    autoIncrement?.let {
                        methodBuilder.addCode(autoIncrement.getContentValuesStatement(isModelContainerAdapter))
                    }
                }

                methodBuilder.addStatement("bindToInsertValues(\$L, \$L)", PARAM_CONTENT_VALUES, ModelUtils.variable)
                if (implementsContentValuesListener) {
                    methodBuilder.addStatement("\$L.onBindTo\$LValues(\$L)",
                            ModelUtils.variable, if (isInsert) "Insert" else "Content", PARAM_CONTENT_VALUES)
                }
            }

            return methodBuilder.build()
        }

    companion object {
        val PARAM_CONTENT_VALUES = "values"
    }
}
