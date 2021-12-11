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
        return PropertySpec.builder(
            model.fieldWrapperName,
            if (model.classType.isNullable) {
                ClassNames.nullablePropertyStatementWrapper(model.classType)
            } else {
                ClassNames.propertyStatementWrapper(model.classType)
            },
            KModifier.PRIVATE,
        )
            .apply {
                if (model.classType.isNullable) {
                    initializer(
                        """
                        %T(
                          { model, statement, index -> statement.%M(index, model.%L) },
                          { statement, index -> statement.bindNull(index) }
                        )
                    """.trimIndent(),
                        ClassNames.nullablePropertyStatementWrapper(model.classType),
                        MemberNames.bind,
                        model.name.shortName,
                    )
                } else {
                    initializer(
                        """
                        %T { model, statement, index -> statement.%M(index, model.%L) }
                    """.trimIndent(),
                        ClassNames.propertyStatementWrapper(model.classType),
                        MemberNames.bind,
                        model.name.shortName,
                    )
                }
            }
            .build()
    }
}


val FieldModel.fieldWrapperName get() = "${name.shortName}_wrapper"