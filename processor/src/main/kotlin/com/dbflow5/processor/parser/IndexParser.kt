package com.dbflow5.processor.parser

import com.dbflow5.annotation.Index
import com.dbflow5.codegen.model.properties.IndexProperties
import com.dbflow5.codegen.parser.Parser

class IndexParser : Parser<Index, IndexProperties> {
    override fun parse(input: Index): IndexProperties {
        return IndexProperties(
            groups = input.indexGroups.toList(),
        )
    }
}
