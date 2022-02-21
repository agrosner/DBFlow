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

class ClassAdapterWriter(
    private val nameAllocator: NameAllocator,
    private val referencesCache: ReferencesCache,
) : TypeCreator<ClassModel, FunSpec> {
    override fun create(model: ClassModel): FunSpec {
        val name = this.nameAllocator.newName(model.generatedFieldName)
        val adapters = model.distinctAdapterGetters(referencesCache)
        return FunSpec.builder(
            "${name}_${
                when (model.type) {
                    is ClassModel.Type.Table -> "adapter"
                    is ClassModel.Type.Query -> "queryAdapter"
                    is ClassModel.Type.View -> "viewAdapter"
                }
            }"
        )
            .addModifiers(if (model.isInternal) KModifier.INTERNAL else KModifier.PUBLIC)
            .apply {
                returns(
                    when (model.type) {
                        is ClassModel.Type.Table -> ClassNames.modelAdapter2(model.classType)
                        is ClassModel.Type.Query -> ClassNames.queryAdapter2(model.classType)
                        is ClassModel.Type.View -> ClassNames.viewAdapter2(model.classType)
                    }
                )
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
                    "return %M(ops = ${model.generatedFieldName}_%L(%L${adapters.joinToString { "%L" }}), " +
                        (if (model.isNormal) "propertyGetter = ${model.generatedFieldName}_propertyGetter" else "") + ")",
                    when (model.type) {
                        is ClassModel.Type.Table -> MemberNames.modelAdapter
                        is ClassModel.Type.View -> MemberNames.viewAdapter
                        is ClassModel.Type.Query -> MemberNames.queryAdapter
                    },
                    when (model.type) {
                        is ClassModel.Type.Table -> "ops"
                        is ClassModel.Type.View, is ClassModel.Type.Query -> "queryOps"
                    },
                    if (model.granularNotifications) "notifyDistributor, " else "",
                    *adapters.map { it.generatedFieldName }.toTypedArray(),
                )
            }
            .build()
    }
}