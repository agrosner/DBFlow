package com.dbflow5.processor.parser

import com.dbflow5.annotation.IndexGroup
import com.dbflow5.codegen.model.properties.IndexGroupProperties
import com.dbflow5.codegen.parser.Parser

class IndexGroupParser : Parser<IndexGroup, IndexGroupProperties> {

    override fun parse(input: IndexGroup): IndexGroupProperties {
        return IndexGroupProperties(
            number = input.number,
            name = input.name,
            unique = input.unique,
        )
    }
}
