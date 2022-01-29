package com.dbflow5.codegen.shared.parser

import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.interop.ClassNameResolver
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.codegen.shared.validation.ValidationExceptionProvider
import com.squareup.kotlinpoet.ClassName

/**
 * Description: Extracts valid field types.
 */
abstract class FieldSanitizer : Parser<ClassDeclaration,
    List<FieldModel>> {

    lateinit var resolver: ClassNameResolver

    fun applyResolver(resolver: ClassNameResolver) {
        this.resolver = resolver
    }

    @Throws(ValidationException::class)
    override fun parse(input: ClassDeclaration): List<FieldModel> = this.parse(input)

    sealed interface Validation : ValidationExceptionProvider {

        data class OnlyOneKind(
            val className: ClassName,
        ) : Validation {
            override val message: String = "$className can only contain one of " +
                "the following types: Table, ModelView, or QueryModel"
        }
    }
}