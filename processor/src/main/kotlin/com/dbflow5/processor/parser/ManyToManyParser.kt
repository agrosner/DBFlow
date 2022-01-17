package com.dbflow5.processor.parser

import com.dbflow5.annotation.ManyToMany
import com.dbflow5.codegen.model.ManyToManyModel
import com.dbflow5.codegen.model.NameModel
import com.dbflow5.codegen.parser.Parser
import com.dbflow5.codegen.parser.validation.ValidationException
import com.dbflow5.processor.interop.KaptClassType
import com.dbflow5.processor.interop.KaptOriginatingFileType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.Element

/**
 * Description:
 */
class ManyToManyParser(
    private val manyToManyPropertyParser: ManyToManyPropertyParser,
) : Parser<ManyToManyParser.Input, ManyToManyModel> {

    data class Input(
        val manyToMany: ManyToMany,
        val name: NameModel,
        val classType: ClassName,
        val databaseTypeName: TypeName,
        val element: Element,
    )

    @Throws(ValidationException::class)
    override fun parse(input: Input): ManyToManyModel {
        return ManyToManyModel(
            name = input.name,
            properties = manyToManyPropertyParser.parse(input.manyToMany),
            classType = input.classType,
            databaseTypeName = input.databaseTypeName,
            ksType = KaptClassType(input.element.asType(), input.element),
            originatingFile = KaptOriginatingFileType,
        )
    }
}