package com.dbflow5.query.list

import android.widget.ListView
import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.config.FlowLog
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.ModelQueriable
import kotlinx.coroutines.runBlocking

/**
 * Interface for callbacks when cursor gets refreshed.
 */
typealias OnCursorRefreshListener<T> = (cursorList: FlowCursorList<T>) -> Unit

/**
 * Description: A non-modifiable, cursor-backed list that you can use in [ListView] or other data sources.
 */
class FlowCursorList<T : Any> private constructor(builder: Builder<T>) : IFlowCursorIterator<T> {

    val table = builder.adapter
    val modelQueriable = builder.modelQueriable
    val databaseWrapper = builder.databaseWrapper
    private var _cursor: FlowCursor? = null
    private val cursorFunc: () -> FlowCursor

    override var trackingCursor: Boolean = false
        private set

    internal val adapter: RetrievalAdapter<T>

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
                runBlocking { adapter.listModelLoader.convertToData(cursor, databaseWrapper) }
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
        trackingCursor = builder.cursor != null
        cursorFunc = {
            builder.cursor
                ?: modelQueriable.cursor(databaseWrapper)
                ?: throw IllegalStateException("The object must evaluate to a cursor")
        }
        adapter = builder.adapter
    }

    override val isClosed: Boolean
        get() = _cursor?.isClosed ?: true

    override operator fun iterator(): FlowCursorIterator<T> =
        FlowCursorIterator(databaseWrapper, this)

    override fun iterator(startingLocation: Long, limit: Long): FlowCursorIterator<T> =
        FlowCursorIterator(databaseWrapper, this, startingLocation, limit)

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
        trackingCursor = false
        synchronized(cursorRefreshListenerSet) {
            cursorRefreshListenerSet.forEach { listener -> listener(this) }
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
            runBlocking {
                adapter.singleModelLoader.convertToData(
                    FlowCursor.from(cursor), false,
                    databaseWrapper
                )
            }
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
    class Builder<T : Any>(
        internal val modelQueriable: ModelQueriable<T>,
        internal val databaseWrapper: DatabaseWrapper
    ) {

        internal val adapter = modelQueriable.adapter
        internal var cursor: FlowCursor? = null

        fun cursor(cursor: FlowCursor?) = apply {
            this.cursor = cursor
        }

        fun build() = FlowCursorList(this)
    }
}
