package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.dbflow5.quoteIfNeeded
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.NameAllocator
import com.squareup.kotlinpoet.PropertySpec

class PropertyGetterWriter(
    private val nameAllocator: NameAllocator,
    private val referencesCache: ReferencesCache,
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
) : TypeCreator<ClassModel, PropertySpec> {

    override fun create(model: ClassModel): PropertySpec =
        PropertySpec.builder(
            "${model.adapterSymbol(nameAllocator)}_propertyGetter",
            ClassNames.propertyGetter(model.classType),
            KModifier.INTERNAL,
        )
            .apply {
                model.originatingSource?.let {
                    originatingFileTypeSpecAdder.addOriginatingFileType(this, it)
                }
            }
            .initializer(
                CodeBlock.builder()
                    .apply {
                        addStatement("{ columnName ->")
                        beginControlFlow(
                            "when(%N.%M())",
                            "columnName",
                            MemberNames.quoteIfNeeded
                        )
                        model.flattenedFields(referencesCache).forEach { field ->
                            add(
                                """
                    %S -> %T.%L
                    
                """.trimIndent(),
                                field.dbName.quoteIfNeeded(),
                                ClassNames.generatedAdapterCompanion(model.classType),
                                field.propertyName,
                            )
                        }
                        add(
                            """
                else -> throw %T(%P) 
            """.trimIndent(),
                            ClassNames.IllegalArgumentException,
                            "Invalid column name passed \$columnName. Ensure you are calling the correct table's column"
                        )
                        endControlFlow()
                        addStatement("}")
                    }
                    .build()
            )
            .build()
}