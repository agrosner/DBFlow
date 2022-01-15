package com.dbflow5.processor.parser

import com.dbflow5.annotation.OneToManyRelation
import com.dbflow5.codegen.model.properties.OneToManyProperties
import com.dbflow5.codegen.parser.Parser
import com.dbflow5.processor.utils.extractClassNameFromAnnotation
import com.squareup.kotlinpoet.javapoet.toKClassName

class OneToManyPropertyParser : Parser<OneToManyRelation,
    OneToManyProperties> {
    override fun parse(input: OneToManyRelation): OneToManyProperties {
        return OneToManyProperties(
            childTableType = input.extractClassNameFromAnnotation { it.childTable }.toKClassName(),
            name = input.generatedClassName,
            parentFieldName = input.parentFieldName,
            childListFieldName = input.childListFieldName,
        )
    }
}
