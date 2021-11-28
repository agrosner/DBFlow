package com.dbflow5.ksp.parser

/**
 * Description:
 */
interface Parser<In, Out> {

    fun parse(input: In): Out
}
