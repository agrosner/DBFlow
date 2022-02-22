package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.distinctAdapterGetters
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.properties.CreatableScopeProperties
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.NameAllocator
import com.squareup.kotlinpoet.TypeName

private data class ClassConfig(
    val fieldName: String,
    val adapterType: TypeName,
    val adapterCreator: MemberName,
    val opsName: String,
)

class ClassAdapterWriter(
    private val nameAllocator: NameAllocator,
    private val referencesCache: ReferencesCache,
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
) : TypeCreator<ClassModel, FunSpec> {
    override fun create(model: ClassModel): FunSpec {
        val name = nameAllocator[model.generatedClassName]
        val adapters = model.distinctAdapterGetters(referencesCache)
        val config = createConfig(model)
        return FunSpec.builder(
            "${name}_${config.fieldName}"
        )
            .addModifiers(if (model.isInternal) KModifier.INTERNAL else KModifier.PUBLIC)
            .apply {
                model.originatingSource?.let {
                    originatingFileTypeSpecAdder.addOriginatingFileType(this, it)
                }
                returns(config.adapterType)
                adapters.forEach { adapter ->
                    addParameter(
                        "${adapter.generatedFieldName}Getter",
                        LambdaTypeName.get(returnType = adapter.generatedSuperClass)
                    )
                }
                addCode(
                    "return %M(ops = ${model.generatedFieldName}_%L(${adapters.joinToString { "%L" }}), \n",
                    config.adapterCreator,
                    config.opsName,
                    *adapters.map { "${it.generatedFieldName}Getter" }.toTypedArray(),
                )
                if (!model.isQuery) {
                    addCode("name = %S, \n", model.dbName)
                    addCode(
                        "createWithDatabase = %L, \n",
                        (model.properties as CreatableScopeProperties).createWithDatabase
                    )
                }
                if (model.isNormal) {
                    addCode("propertyGetter = ${model.generatedFieldName}_propertyGetter, \n")
                    addCode("creationSQL = %L", "${model.generatedFieldName}_creationSQL, \n")
                    addCode("primaryModelClauseGetter = ${model.generatedFieldName}_primaryModelClauseGetter, \n")
                } else if (model.isView) {
                    addCode("creationLoader = %L", "${model.generatedFieldName}_creationLoader, \n")
                }
                addCode(")")
            }
            .build()
    }

    private fun createConfig(model: ClassModel): ClassConfig = when (
        model.type
    ) {
        is ClassModel.Type.Table ->
            ClassConfig(
                fieldName = "adapter",
                adapterType = ClassNames.modelAdapter2(model.classType),
                adapterCreator = MemberNames.modelAdapter,
                opsName = "ops",
            )
        is ClassModel.Type.View -> ClassConfig(
            fieldName = "viewAdapter",
            adapterType = ClassNames.viewAdapter2(model.classType),
            adapterCreator = MemberNames.viewAdapter,
            opsName = "queryOps",
        )
        is ClassModel.Type.Query -> ClassConfig(
            fieldName = "queryAdapter",
            adapterType = ClassNames.queryAdapter2(model.classType),
            adapterCreator = MemberNames.queryAdapter,
            opsName = "queryOps",
        )
    }
}