package com.dbflow5.model.writer

/**
 * Description:
 */
fun interface TypeCreator<In, Out> {

    fun create(model: In): Out
}
