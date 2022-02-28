package com.dbflow5.processor.parser

import com.dbflow5.annotation.OneToMany
import com.dbflow5.codegen.shared.properties.OneToManyProperties
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.processor.utils.extractClassNameFromAnnotation
import com.squareup.kotlinpoet.javapoet.toKClassName

class OneToManyPropertyParser : Parser<OneToMany,
    OneToManyProperties> {
    override fun parse(input: OneToMany): OneToManyProperties {
        return OneToManyProperties(
            childTableType = input.extractClassNameFromAnnotation { it.childTable }.toKClassName(),
            name = input.generatedClassName,
            parentFieldName = input.parentFieldName,
            childListFieldName = input.childListFieldName,
        )
    }
}
