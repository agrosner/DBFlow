package com.dbflow5.codegen.writer

/**
 * Description:
 */
fun interface TypeCreator<In, Out> {

    fun create(model: In): Out
}
