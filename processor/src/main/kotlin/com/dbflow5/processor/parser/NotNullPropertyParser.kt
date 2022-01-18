package com.dbflow5.processor.parser

import com.dbflow5.annotation.NotNull
import com.dbflow5.codegen.shared.properties.NotNullProperties
import com.dbflow5.codegen.shared.parser.Parser

class NotNullPropertyParser : Parser<NotNull, NotNullProperties> {
    override fun parse(input: NotNull): NotNullProperties {
        return NotNullProperties(
            conflictAction = input.onNullConflict,
        )
    }
}
