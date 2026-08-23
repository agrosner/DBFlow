package com.dbflow5.compiler.ir

import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.generateTypeConverter
import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.parser.FieldSanitizer
import com.dbflow5.codegen.shared.validation.ValidationException

internal class IrFieldSanitizer(
    private val propertyParser: IrPropertyParser,
    private val typeConverterCache: TypeConverterCache,
) : FieldSanitizer() {

    @Throws(ValidationException::class)
    override fun parse(input: ClassDeclaration): List<FieldModel> {
        val declaration = (input as IrClassDeclaration).irClass
        val isTable = declaration.hasAnnotation(IrTypeFqNames.Table)
        val isModelView = declaration.hasAnnotation(IrTypeFqNames.ModelView)
        val isQuery = declaration.hasAnnotation(IrTypeFqNames.Query)
        if (listOf(isTable, isModelView, isQuery).count { it } > 1) {
            throw Validation.OnlyOneKind(declaration.toPoetClassName()).exception
        }
        return declaration.allProperties()
            .filterNot { it.isAbstract }
            .filterNot { it.isIgnoredColumn }
            .filterNot { property -> property.isDelegated }
            .distinctBy { it.name.asString() }
            .map(propertyParser::parse)
            .toList()
            .also { list ->
                list.filter { it.isInlineClass }.forEach { inlineType ->
                    typeConverterCache.putGeneratedTypeConverter(
                        inlineType.generateTypeConverter(resolver)
                    )
                }
            }
    }
}
