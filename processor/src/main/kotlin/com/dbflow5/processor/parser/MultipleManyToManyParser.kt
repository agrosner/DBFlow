package com.dbflow5.processor.parser

import com.dbflow5.annotation.MultipleManyToMany
import com.dbflow5.codegen.shared.ManyToManyModel
import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.parser.Parser
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Description:
 */
class MultipleManyToManyParser(
    private val manyToManyParser: ManyToManyParser,
) : Parser<MultipleManyToManyParser.Input, List<ManyToManyModel>> {
    data class Input(
        val annotation: MultipleManyToMany,
        val name: NameModel,
        val classType: ClassName,
        val databaseTypeName: TypeName,
        val element: TypeElement,
    )

    override fun parse(input: Input): List<ManyToManyModel> {
        return input.annotation.value.map {
            manyToManyParser.parse(
                ManyToManyParser.Input(
                    manyToMany = it,
                    name = input.name,
                    classType = input.classType,
                    databaseTypeName = input.databaseTypeName,
                    element = input.element,
                )
            )
        }
    }
}