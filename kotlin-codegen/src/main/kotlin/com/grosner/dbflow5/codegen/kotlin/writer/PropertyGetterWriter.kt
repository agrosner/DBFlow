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
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName

class PropertyGetterWriter(
    private val referencesCache: ReferencesCache,
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
) : TypeCreator<ClassModel, PropertySpec> {

    override fun create(model: ClassModel): PropertySpec =
        PropertySpec.builder(
            "${model.generatedFieldName}_propertyGetter",
            ClassNames.propertyGetter(model.classType),
            KModifier.PRIVATE,
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
                    %S -> %L.%L
                    
                """.trimIndent(),
                                field.dbName.quoteIfNeeded(),
                                model.generatedClassName.className,
                                field.propertyName,
                            )
                        }
                        add(
                            """
                else -> throw %T(%P) 
            """.trimIndent(),
                            IllegalArgumentException::class.asClassName(),
                            "Invalid column name passed \$columnName. Ensure you are calling the correct table's column"
                        )
                        endControlFlow()
                        addStatement("}")
                    }
                    .build()
            )
            .build()
}