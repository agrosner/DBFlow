package com.dbflow5.codegen.parser

import com.dbflow5.codegen.parser.validation.ValidationException

/**
 * Description:
 */
interface Parser<In, Out> {

    @Throws(ValidationException::class)
    fun parse(input: In): Out
}