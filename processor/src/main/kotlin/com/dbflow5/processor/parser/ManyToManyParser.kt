package com.dbflow5.processor.parser

import com.dbflow5.annotation.ManyToMany
import com.dbflow5.codegen.shared.ManyToManyModel
import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.processor.interop.KaptOriginatingSource
import com.dbflow5.processor.interop.KaptTypeElementClassType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.TypeElement

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
        val element: TypeElement,
    )

    @Throws(ValidationException::class)
    override fun parse(input: Input): ManyToManyModel {
        return ManyToManyModel(
            name = input.name,
            properties = manyToManyPropertyParser.parse(input.manyToMany),
            classType = input.classType,
            databaseTypeName = input.databaseTypeName,
            ksType = KaptTypeElementClassType(input.element.asType(), input.element),
            originatingSource = KaptOriginatingSource(input.element),
        )
    }
}