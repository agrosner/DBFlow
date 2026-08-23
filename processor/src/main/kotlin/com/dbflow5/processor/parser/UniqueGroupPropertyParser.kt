package com.dbflow5.processor.parser

import com.dbflow5.annotation.UniqueGroup
import com.dbflow5.codegen.shared.properties.UniqueGroupProperties
import com.dbflow5.codegen.shared.parser.Parser

class UniqueGroupPropertyParser : Parser<UniqueGroup, UniqueGroupProperties> {
    override fun parse(input: UniqueGroup): UniqueGroupProperties {
        return UniqueGroupProperties(
            number = input.groupNumber,
            conflictAction = input.uniqueConflict,
        )
    }
}
