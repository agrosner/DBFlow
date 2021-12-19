package com.dbflow5.ksp.writer

/**
 * Description:
 */
fun interface TypeCreator<In, Out> {

    fun create(model: In): Out
}
