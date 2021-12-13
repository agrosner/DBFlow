package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.model.FieldModel
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

/**
 * Description:
 */
class PropertyStatementWrapperWriter : TypeCreator<FieldModel, PropertySpec> {

    override fun create(model: FieldModel): PropertySpec {
        val type = if (model.classType.isNullable) {
            ClassNames.nullablePropertyStatementWrapper(model.classType)
        } else {
            ClassNames.propertyStatementWrapper(model.classType)
        }
        return PropertySpec.builder(
            model.fieldWrapperName,
            type,
            KModifier.PRIVATE,
        )
            .apply {
                initializer(
                    """
                        %T { data, statement, index -> statement.%M(index, data) }
                    """.trimIndent(),
                    type,
                    MemberNames.bind,
                )
            }
            .build()
    }
}


val FieldModel.fieldWrapperName get() = "${name.shortName}_wrapper"