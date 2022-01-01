package com.dbflow5.ksp.parser

import com.dbflow5.ksp.parser.validation.ValidationException

/**
 * Description:
 */
interface Parser<In, Out> {

    @Throws(ValidationException::class)
    fun parse(input: In): Out
}
