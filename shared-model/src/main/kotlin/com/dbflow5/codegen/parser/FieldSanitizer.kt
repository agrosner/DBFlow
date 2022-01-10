package com.dbflow5.codegen.parser

import com.dbflow5.codegen.model.FieldModel
import com.dbflow5.codegen.model.interop.ClassDeclaration
import com.dbflow5.codegen.parser.Parser
import com.dbflow5.codegen.parser.validation.ValidationException

/**
 * Description: Extracts valid field types.
 */
interface FieldSanitizer : Parser<ClassDeclaration,
    List<FieldModel>> {

    @Throws(ValidationException::class)
    override fun parse(input: ClassDeclaration): List<FieldModel>

}