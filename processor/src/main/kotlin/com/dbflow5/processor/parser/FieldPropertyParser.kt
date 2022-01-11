package com.dbflow5.processor.parser

import com.dbflow5.annotation.Column
import com.dbflow5.codegen.model.properties.FieldProperties
import com.dbflow5.codegen.parser.Parser
import com.squareup.kotlinpoet.asClassName

class FieldPropertyParser : Parser<Column, FieldProperties> {
    override fun parse(input: Column): FieldProperties {
        return FieldProperties(
            name = input.name,
            length = input.length,
            collate = input.collate,
            defaultValue = input.defaultValue,
            typeConverterClassName = input.typeConverter.asClassName(),
        )
    }
}