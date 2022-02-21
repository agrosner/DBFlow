package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

class TableOpsFunWriter : TypeCreator<ClassModel, FunSpec> {

    override fun create(model: ClassModel): FunSpec {
        val shortName = model.generatedFieldName
        return FunSpec.builder(
            "${shortName}_ops",
        )
            .addModifiers(KModifier.PRIVATE)
            .apply {
                if (model.granularNotifications) {
                    addParameter(
                        "notifyDistributor", ClassNames.NotifyDistributor
                            .copy(nullable = true)
                    )
                }
                returns(ClassNames.tableOps(model.classType))
                addCode(
                    "return %T(tableSQL = ${shortName}_sql, " +
                        "tableBinder = ${shortName}_tableBinder, " +
                        "primaryModelClauseGetter = ${shortName}_primaryModelClauseGetter, " +
                        "autoIncrementUpdater = ${shortName}_autoIncrementUpdater, " +
                        "notifyDistributorGetter = %L)",
                    ClassNames.TableOpsImpl,
                    if (model.granularNotifications) "notifyDistributor" else "{ null }",
                )
            }
            .build()
    }
}