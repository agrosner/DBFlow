package com.dbflow5.codegen.shared.parser

import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.validation.FieldValidator
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.codegen.shared.validation.ValidationExceptionProvider
import com.squareup.kotlinpoet.ClassName

/**
 * Description: Extracts valid field types.
 */
abstract class FieldSanitizer(
    private val fieldValidator: FieldValidator,
) : Parser<ClassDeclaration,
    List<FieldModel>> {

    @Throws(ValidationException::class)
    final override fun parse(input: ClassDeclaration): List<FieldModel> {
        val fields = parseFields(input)
        fields.forEach { fieldValidator.validate(it) }
        return fields
    }

    abstract fun parseFields(input: ClassDeclaration): List<FieldModel>

    sealed interface Validation : ValidationExceptionProvider {

        data class OnlyOneKind(
            val className: ClassName,
        ) : Validation {
            override val message: String = "$className can only contain one of " +
                "the following types: Table, ModelView, or QueryModel"
        }
    }
}