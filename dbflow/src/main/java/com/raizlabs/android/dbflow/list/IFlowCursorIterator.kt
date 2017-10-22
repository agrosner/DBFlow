package com.raizlabs.android.dbflow.list

import android.database.Cursor

import java.io.Closeable
import java.io.IOException

/**
 * Description: Simple interface that allows you to iterate a [Cursor].
 */
interface IFlowCursorIterator<TModel> : Closeable, Iterable<TModel> {

    /**
     * @return Count of the [Cursor].
     */
    val count: Long

    /**
     * @param position The position within the [Cursor] to retrieve and convert into a [TModel]
     */
    fun getItem(position: Long): TModel

    /**
     * @return The cursor.
     */
    fun cursor(): Cursor?

    override fun iterator(): FlowCursorIterator<TModel>

    /**
     * @return Can iterate the [Cursor]. Specifies a starting location + count limit of results.
     */
    fun iterator(startingLocation: Int, limit: Long): FlowCursorIterator<TModel>

    @Throws(IOException::class)
    override fun close()
}
