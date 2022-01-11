package com.dbflow5.processor.parser

import com.dbflow5.annotation.OneToManyRelation
import com.dbflow5.codegen.model.properties.OneToManyProperties
import com.dbflow5.codegen.parser.Parser
import com.squareup.kotlinpoet.asClassName

class OneToManyPropertyParser : Parser<OneToManyRelation,
    OneToManyProperties> {
    override fun parse(input: OneToManyRelation): OneToManyProperties {
        return OneToManyProperties(
            childTableType = input.childTable.asClassName(),
            name = input.generatedClassName,
            parentFieldName = input.parentFieldName,
            childListFieldName = input.childListFieldName,
        )
    }
}
