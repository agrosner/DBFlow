package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.distinctAdapterGetters
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.NameAllocator

class ModelAdapterWriter(
    private val nameAllocator: NameAllocator,
    private val referencesCache: ReferencesCache,
) : TypeCreator<ClassModel, FunSpec> {
    override fun create(model: ClassModel): FunSpec {
        val name = this.nameAllocator.newName(model.generatedFieldName)
        val adapters = model.distinctAdapterGetters(referencesCache)
        return FunSpec.builder("${name}_adapter")
            .addModifiers(if (model.isInternal) KModifier.INTERNAL else KModifier.PUBLIC)
            .apply {
                returns(ClassNames.modelAdapter2(model.classType))
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
                addCode(
                    "return %M(ops = ${model.generatedFieldName}_ops(%L${adapters.joinToString { "%L" }}), " +
                        "propertyGetter = ${model.generatedFieldName}_propertyGetter)",
                    MemberNames.modelAdapter,
                    if (model.granularNotifications) "notifyDistributor, " else "",
                    *adapters.map { it.generatedFieldName }.toTypedArray(),
                )
            }
            .build()
    }
}