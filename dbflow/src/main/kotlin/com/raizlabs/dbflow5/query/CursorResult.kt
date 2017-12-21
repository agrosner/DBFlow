package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.adapter.RetrievalAdapter
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.config.queryModelAdapter
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.query.list.FlowCursorIterator
import com.raizlabs.dbflow5.query.list.IFlowCursorIterator

/**
 * Description: A class that contains a [FlowCursor] and handy methods for retrieving data from it.
 * You must close this object post use via [.close].
 */
class CursorResult<T : Any> internal constructor(modelClass: Class<T>, cursor: FlowCursor?,
                                                 private val databaseWrapper: DatabaseWrapper)
    : IFlowCursorIterator<T> {

    private val retrievalAdapter: RetrievalAdapter<T>

    private var _cursor: FlowCursor? = null

    override val count: Long
        get() = _cursor?.count?.toLong() ?: 0

    init {
        if (cursor != null) {
            this._cursor = FlowCursor.from(cursor)
        }
        retrievalAdapter = FlowManager.getRetrievalAdapter(modelClass)
    }

    /**
     * Swaps the current cursor and will close existing one.
     */
    fun swapCursor(cursor: FlowCursor?) {
        this._cursor?.let { _cursor ->
            if (!_cursor.isClosed) {
                _cursor.close()
            }
        }
        this._cursor = cursor
    }

    /**
     * @return A [List] of items from this object. You must call [.close] when finished.
     */
    fun toList(): List<T> = _cursor?.let { cursor ->
        retrievalAdapter.listModelLoader.convertToData(cursor, databaseWrapper)
    } ?: arrayListOf()

    /**
     * @return Converts the [FlowCursor] to a [List] of [T] and then closes it.
     */
    fun toListClose(): List<T> {
        val list = retrievalAdapter.listModelLoader.load(_cursor, databaseWrapper) ?: arrayListOf()
        close()
        return list
    }

    /**
     * @return A [List] of items from this object. You must call [.close] when finished.
     */
    fun <TCustom : Any> toCustomList(customClass: Class<TCustom>): List<TCustom> {
        return _cursor?.let { cursor ->
            return@let customClass.queryModelAdapter
                .listModelLoader.convertToData(cursor, databaseWrapper)
        } ?: arrayListOf()
    }

    /**
     * @return Converts the [FlowCursor] to a [List] of [T] and then closes it.
     */
    fun <TCustom : Any> toCustomListClose(customClass: Class<TCustom>): List<TCustom> {
        val customList = customClass.queryModelAdapter.listModelLoader
            .load(_cursor, databaseWrapper) ?: arrayListOf()
        close()
        return customList
    }

    /**
     * @return The first [T] of items from the contained [FlowCursor]. You must call [.close] when finished.
     */
    fun toModel(): T? = _cursor?.let { cursor ->
        retrievalAdapter.singleModelLoader.convertToData(cursor, databaseWrapper)
    }

    /**
     * @return Converts the [FlowCursor] into the first [T] from the cursor and then closes it.
     */
    fun toModelClose(): T? {
        val model = retrievalAdapter.singleModelLoader.load(_cursor, databaseWrapper)
        close()
        return model
    }

    /**
     * @return The first [T] of items from the contained [FlowCursor]. You must call [.close] when finished.
     */
    fun <TCustom : Any> toCustomModel(customClass: Class<TCustom>): TCustom? {
        return _cursor?.let { _cursor ->
            customClass.queryModelAdapter.singleModelLoader.convertToData(_cursor, databaseWrapper)
        }
    }

    /**
     * @return Converts the [FlowCursor] to a [T] and then closes it.
     */
    fun <TCustom : Any> toCustomModelClose(customClass: Class<TCustom>): TCustom? {
        val customList = customClass.queryModelAdapter.singleModelLoader.load(_cursor, databaseWrapper)
        close()
        return customList
    }

    override fun get(index: Long): T {
        var model: T? = null
        _cursor?.let { cursor ->
            if (cursor.moveToPosition(index.toInt())) {
                model = retrievalAdapter.singleModelLoader.convertToData(cursor, false, databaseWrapper)
            } else {
                throw IndexOutOfBoundsException("Cursor failed to move to position $index")
            }
        } ?: throw IllegalStateException("Cursor is no longer open on this CursorResult or no Cursor found.")
        return model ?: throw IllegalStateException("RetrievalAdapter did not return a model for index $index")
    }

    override fun iterator(): FlowCursorIterator<T> = FlowCursorIterator(this)

    override fun iterator(startingLocation: Int, limit: Long): FlowCursorIterator<T> =
        FlowCursorIterator(this, startingLocation, limit)

    override val cursor: FlowCursor?
        get() = _cursor

    override fun close() {
        _cursor?.close()
        _cursor = null
    }
}

inline fun <reified T : Any> CursorResult<*>.toCustomList() = toCustomList(T::class.java)

inline fun <reified T : Any> CursorResult<*>.toCustomListClose() = toCustomListClose(T::class.java)

inline fun <reified T : Any> CursorResult<*>.toCustomModel() = toCustomModel(T::class.java)

inline fun <reified T : Any> CursorResult<*>.toCustomModelClose() = toCustomModelClose(T::class.java)
