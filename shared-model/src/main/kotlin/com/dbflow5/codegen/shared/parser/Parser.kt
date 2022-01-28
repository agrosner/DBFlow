package com.dbflow5.codegen.shared.parser

import com.dbflow5.codegen.shared.validation.ValidationException

/**
 * Description:
 */
interface Parser<In, Out> {

    @Throws(ValidationException::class)
    fun parse(input: In): Out
}