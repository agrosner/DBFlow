package com.raizlabs.dbflow5.query.list

import android.widget.ListView
import com.raizlabs.dbflow5.adapter.RetrievalAdapter
import com.raizlabs.dbflow5.config.FlowLog
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.query.ModelQueriable

/**
 * Description: A non-modifiable, cursor-backed list that you can use in [ListView] or other data sources.
 */
class FlowCursorList<T : Any> private constructor(builder: Builder<T>) : IFlowCursorIterator<T> {

    /**
     * Interface for callbacks when cursor gets refreshed.
     */
    interface OnCursorRefreshListener<TModel : Any> {

        /**
         * Callback when cursor refreshes.
         *
         * @param cursorList The object that changed.
         */
        fun onCursorRefreshed(cursorList: FlowCursorList<TModel>)
    }

    val table: Class<T>
    val modelQueriable: ModelQueriable<T>
    private var _cursor: FlowCursor? = null
    private val cursorFunc: () -> FlowCursor
    val databaseWrapper: DatabaseWrapper

    internal val instanceAdapter: RetrievalAdapter<T>

    private val cursorRefreshListenerSet = hashSetOf<OnCursorRefreshListener<T>>()

    /**
     * @return the full, converted [T] list from the database on this list. For large
     * data sets that require a large conversion, consider calling this on a BG thread.
     */
    val all: List<T>
        get() {
            unpackCursor()
            throwIfCursorClosed()
            warnEmptyCursor()
            return _cursor?.let { cursor ->
                instanceAdapter.listModelLoader.convertToData(cursor, databaseWrapper)
            } ?: listOf()
        }

    /**
     * @return the count of rows on this database query list.
     */
    val isEmpty: Boolean
        get() {
            throwIfCursorClosed()
            warnEmptyCursor()
            return count == 0L
        }

    init {
        table = builder.modelClass
        this.modelQueriable = builder.modelQueriable
        this.databaseWrapper = builder.databaseWrapper
        cursorFunc = {
            builder.cursor
                ?: modelQueriable.cursor(databaseWrapper)
                ?: throw IllegalStateException("The cursor must evaluate to a cursor")
        }
        instanceAdapter = FlowManager.getRetrievalAdapter(builder.modelClass)
    }

    override operator fun iterator(): FlowCursorIterator<T> = FlowCursorIterator(this)

    override fun iterator(startingLocation: Int, limit: Long): FlowCursorIterator<T> =
        FlowCursorIterator(this, startingLocation, limit)

    /**
     * Register listener for when cursor refreshes.
     */
    fun addOnCursorRefreshListener(onCursorRefreshListener: OnCursorRefreshListener<T>) {
        synchronized(cursorRefreshListenerSet) {
            cursorRefreshListenerSet.add(onCursorRefreshListener)
        }
    }

    fun removeOnCursorRefreshListener(onCursorRefreshListener: OnCursorRefreshListener<T>) {
        synchronized(cursorRefreshListenerSet) {
            cursorRefreshListenerSet.remove(onCursorRefreshListener)
        }
    }

    /**
     * Refreshes the data backing this list, and destroys the Model cache.
     */
    @Synchronized
    fun refresh() {
        val cursor = unpackCursor()
        cursor.close()
        this._cursor = modelQueriable.cursor(databaseWrapper)
        synchronized(cursorRefreshListenerSet) {
            cursorRefreshListenerSet.forEach { listener -> listener.onCursorRefreshed(this) }
        }
    }

    /**
     * Returns a model at the specified index. If we are using the cache and it does not contain a model
     * at that index, we move the cursor to the specified index and construct the [T].
     *
     * @param index The row number in the [FlowCursor] to look at
     * @return The [T] converted from the cursor
     */
    override fun get(index: Long): T {
        throwIfCursorClosed()

        val cursor = unpackCursor()
        return if (cursor.moveToPosition(index.toInt())) {
            instanceAdapter.singleModelLoader.convertToData(
                FlowCursor.from(cursor), false,
                databaseWrapper)
                ?: throw IndexOutOfBoundsException("Invalid item at index $index. Check your cursor data.")
        } else {
            throw IndexOutOfBoundsException("Invalid item at index $index. Check your cursor data.")
        }
    }

    /**
     * @return the count of the rows in the [FlowCursor] backed by this list.
     */
    override val count: Long
        get() {
            unpackCursor()
            throwIfCursorClosed()
            warnEmptyCursor()
            return (_cursor?.count ?: 0).toLong()
        }

    /**
     * Closes the cursor backed by this list
     */
    override fun close() {
        warnEmptyCursor()
        _cursor?.close()
        _cursor = null
    }

    override val cursor: FlowCursor?
        get() {
            unpackCursor()
            throwIfCursorClosed()
            warnEmptyCursor()
            return _cursor
        }

    private fun unpackCursor(): FlowCursor = _cursor ?: cursorFunc().also { _cursor = it }

    private fun throwIfCursorClosed() {
        if (_cursor?.isClosed == true) {
            throw IllegalStateException("Cursor has been closed for FlowCursorList")
        }
    }

    private fun warnEmptyCursor() {
        if (_cursor == null) {
            FlowLog.log(FlowLog.Level.W, "Cursor was null for FlowCursorList")
        }
    }

    /**
     * @return A new [Builder] that contains the same cache, query statement, and other
     * underlying data, but allows for modification.
     */
    fun newBuilder(): Builder<T> = Builder(modelQueriable, databaseWrapper).cursor(_cursor)

    /**
     * Provides easy way to construct a [FlowCursorList].
     *
     * @param [T]
     */
    class Builder<T : Any>(internal var modelQueriable: ModelQueriable<T>,
                           internal val databaseWrapper: DatabaseWrapper) {

        internal val modelClass: Class<T> = modelQueriable.table
        internal var cursor: FlowCursor? = null

        fun cursor(cursor: FlowCursor?) = apply {
            this.cursor = cursor
        }

        fun build() = FlowCursorList(this)

    }

}
