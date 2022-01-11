package com.dbflow5.processor.parser

import com.dbflow5.annotation.Unique
import com.dbflow5.codegen.model.properties.UniqueProperties
import com.dbflow5.codegen.parser.Parser

class UniquePropertyParser : Parser<Unique, UniqueProperties> {
    override fun parse(input: Unique): UniqueProperties {
        return UniqueProperties(
            unique = input.unique,
            groups = input.uniqueGroups.toList(),
            conflictAction = input.onUniqueConflict,
        )
    }
}
