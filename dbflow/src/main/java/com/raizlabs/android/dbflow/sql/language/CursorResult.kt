package com.raizlabs.android.dbflow.sql.language

import android.database.Cursor
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.list.FlowCursorIterator
import com.raizlabs.android.dbflow.list.IFlowCursorIterator
import com.raizlabs.android.dbflow.structure.InstanceAdapter
import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: A class that contains a [Cursor] and handy methods for retrieving data from it.
 * You must close this object post use via [.close].
 */
class CursorResult<T : Any> internal constructor(modelClass: Class<T>, cursor: Cursor?)
    : IFlowCursorIterator<T> {

    private val retrievalAdapter: InstanceAdapter<T>

    private var cursor: FlowCursor? = null

    override val count: Long
        get() = cursor?.count?.toLong() ?: 0

    init {
        if (cursor != null) {
            this.cursor = FlowCursor.from(cursor)
        }
        retrievalAdapter = FlowManager.getInstanceAdapter(modelClass)
    }

    /**
     * Swaps the current cursor and will close existing one.
     */
    fun swapCursor(cursor: FlowCursor?) {
        this.cursor?.let { _cursor ->
            if (!_cursor.isClosed) {
                _cursor.close()
            }
        }
        this.cursor = cursor
    }

    /**
     * @return A [List] of items from this object. You must call [.close] when finished.
     */
    fun toList(): List<T> = cursor?.let { cursor ->
        retrievalAdapter.listModelLoader.convertToData(cursor, null)
    } ?: arrayListOf()

    /**
     * @return Converts the [Cursor] to a [List] of [T] and then closes it.
     */
    fun toListClose(): List<T> {
        val list = retrievalAdapter.listModelLoader.load(cursor) ?: arrayListOf()
        close()
        return list
    }

    /**
     * @return A [List] of items from this object. You must call [.close] when finished.
     */
    fun <TCustom : Any> toCustomList(customClass: Class<TCustom>): List<TCustom> {
        return cursor?.let { cursor ->
            return@let FlowManager.getQueryModelAdapter(customClass)
                    .listModelLoader.convertToData(cursor, null)
        } ?: arrayListOf()
    }

    /**
     * @return Converts the [Cursor] to a [List] of [T] and then closes it.
     */
    fun <TCustom : Any> toCustomListClose(customClass: Class<TCustom>): List<TCustom> {
        val customList = FlowManager.getQueryModelAdapter(customClass).listModelLoader.load(cursor) ?: arrayListOf()
        close()
        return customList
    }

    /**
     * @return The first [T] of items from the contained [Cursor]. You must call [.close] when finished.
     */
    fun toModel(): T? = cursor?.let { cursor -> retrievalAdapter.singleModelLoader.convertToData(cursor, null) }

    /**
     * @return Converts the [Cursor] into the first [T] from the cursor and then closes it.
     */
    fun toModelClose(): T? {
        val model = retrievalAdapter.singleModelLoader.load(cursor)
        close()
        return model
    }

    /**
     * @return The first [T] of items from the contained [Cursor]. You must call [.close] when finished.
     */
    fun <TCustom : Any> toCustomModel(customClass: Class<TCustom>): TCustom? {
        return if (cursor != null)
            FlowManager.getQueryModelAdapter(customClass)
                    .singleModelLoader.convertToData(cursor!!, null)
        else null
    }

    /**
     * @return Converts the [Cursor] to a [T] and then closes it.
     */
    fun <TCustom : Any> toCustomModelClose(customClass: Class<TCustom>): TCustom? {
        val customList = FlowManager.getQueryModelAdapter(customClass).singleModelLoader.load(cursor)
        close()
        return customList
    }

    override fun get(position: Long): T {
        var model: T? = null
        cursor?.let { cursor ->
            if (cursor.moveToPosition(position.toInt())) {
                model = retrievalAdapter.singleModelLoader.convertToData(cursor, null, false)
            }
        }
        return model!!
    }

    override fun iterator(): FlowCursorIterator<T> = FlowCursorIterator(this)

    override fun iterator(startingLocation: Int, limit: Long): FlowCursorIterator<T> =
            FlowCursorIterator(this, startingLocation, limit)

    override fun cursor(): Cursor? = cursor

    override fun close() {
        cursor?.close()
    }
}

inline fun <reified T : Any> CursorResult<*>.toCustomList() = toCustomList(T::class.java)

inline fun <reified T : Any> CursorResult<*>.toCustomListClose() = toCustomListClose(T::class.java)

inline fun <reified T : Any> CursorResult<*>.toCustomModel() = toCustomModel(T::class.java)

inline fun <reified T : Any> CursorResult<*>.toCustomModelClose() = toCustomModelClose(T::class.java)
