package com.dbflow5.codegen.parser

import com.dbflow5.codegen.model.FieldModel
import com.dbflow5.codegen.model.interop.ClassDeclaration
import com.dbflow5.codegen.parser.Parser
import com.dbflow5.codegen.parser.validation.ValidationException
import com.dbflow5.codegen.parser.validation.ValidationExceptionProvider
import com.squareup.kotlinpoet.ClassName

/**
 * Description: Extracts valid field types.
 */
interface FieldSanitizer : Parser<ClassDeclaration,
    List<FieldModel>> {

    @Throws(ValidationException::class)
    override fun parse(input: ClassDeclaration): List<FieldModel>

    sealed interface Validation : ValidationExceptionProvider {

        data class OnlyOneKind(
            val className: ClassName,
        ) : Validation {
            override val message: String = "$className can only contain one of " +
                "the following types: Table, ModelView, or QueryModel"
        }
    }
}