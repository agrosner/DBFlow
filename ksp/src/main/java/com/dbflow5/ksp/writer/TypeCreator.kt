package com.dbflow5.ksp.writer

/**
 * Description:
 */
interface TypeCreator<In, Out> {

    fun create(model: In): Out
}
