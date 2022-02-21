package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.distinctAdapterGetters
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName

class TableOpsFunWriter(
    private val referencesCache: ReferencesCache,
) : TypeCreator<ClassModel, FunSpec> {

    override fun create(model: ClassModel): FunSpec {
        val shortName = model.generatedFieldName
        val adapters = model.distinctAdapterGetters(referencesCache)
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
                adapters.forEach { adapter ->
                    addParameter(
                        adapter.generatedFieldName,
                        LambdaTypeName.get(returnType = adapter.generatedSuperClass)
                    )
                }
                returns(ClassNames.tableOps(model.classType))
                addCode(
                    "return %T(tableSQL = ${shortName}_sql, " +
                        "tableBinder = ${shortName}_tableBinder, " +
                        "primaryModelClauseGetter = ${shortName}_primaryModelClauseGetter, " +
                        "autoIncrementUpdater = ${shortName}_autoIncrementUpdater, " +
                        "queryOps = ${shortName}_queryOps(${adapters.joinToString { "%L" }})," +
                        "notifyDistributorGetter = %L)",
                    ClassNames.TableOpsImpl,
                    *adapters.map { it.generatedFieldName }.toTypedArray(),
                    if (model.granularNotifications) "notifyDistributor" else "{ null }",
                )
            }
            .build()
    }
}