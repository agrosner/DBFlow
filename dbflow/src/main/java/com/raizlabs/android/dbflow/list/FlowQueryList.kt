package com.raizlabs.android.dbflow.list

import android.database.Cursor
import android.os.Handler
import android.os.Looper
import com.raizlabs.android.dbflow.list.FlowCursorList.OnCursorRefreshListener
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable
import com.raizlabs.android.dbflow.structure.InstanceAdapter
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction

/**
 * Description: Operates very similiar to a [java.util.List] except its backed by a table cursor. All of
 * the [java.util.List] modifications default to the main thread, but it can be set to
 * run on the [DefaultTransactionQueue]. Register a [Transaction.Success]
 * on this list to know when the results complete. NOTE: any modifications to this list will be reflected
 * on the underlying table.
 */
class FlowQueryList<T : Any>(
        /**
         * If true, we will make all modifications on the [DefaultTransactionQueue], else
         * we will run it on the main thread.
         */
        val transact: Boolean = false,
        private var changeInTransaction: Boolean = false,
        /**
         * Holds the table cursor
         */
        val internalCursorList: FlowCursorList<T>)
    : List<T>, IFlowCursorIterator<T> {

    private var pendingRefresh = false

    /**
     * @return a mutable list that does not reflect changes on the underlying DB.
     */
    val copy: List<T>
        get() = internalCursorList.all

    internal val instanceAdapter: InstanceAdapter<T>
        get() = internalCursorList.instanceAdapter

    override val count: Long
        get() = internalCursorList.count

    private val refreshRunnable = object : Runnable {
        override fun run() {
            synchronized(this) {
                pendingRefresh = false
            }
            refresh()
        }
    }

    internal constructor(builder: Builder<T>) : this(
            transact = builder.transact,
            changeInTransaction = builder.changeInTransaction,
            internalCursorList = FlowCursorList.Builder(builder.modelQueriable)
                    .cursor(builder.cursor)
                    .build()
    )

    fun addOnCursorRefreshListener(onCursorRefreshListener: OnCursorRefreshListener<T>) {
        internalCursorList.addOnCursorRefreshListener(onCursorRefreshListener)
    }

    fun removeOnCursorRefreshListener(onCursorRefreshListener: OnCursorRefreshListener<T>) {
        internalCursorList.removeOnCursorRefreshListener(onCursorRefreshListener)
    }

    val cursorList: FlowCursorList<T>
        get() = internalCursorList

    /**
     * @return Constructs a new [Builder] that reuses the underlying [Cursor], cache,
     * callbacks, and other properties.
     */
    fun newBuilder(): Builder<T> {
        return Builder(internalCursorList)
                .changeInTransaction(changeInTransaction)
                .transact(transact)
    }

    /**
     * Refreshes the content backing this list.
     */
    fun refresh() {
        internalCursorList.refresh()
    }

    /**
     * Will refresh content at a slightly later time, and multiple subsequent calls to this method within
     * a short period of time will be combined into one call.
     */
    fun refreshAsync() {
        synchronized(this) {
            if (pendingRefresh) {
                return
            }
            pendingRefresh = true
        }
        REFRESH_HANDLER.post(refreshRunnable)
    }

    /**
     * Checks to see if the table contains the object only if its a [T]
     *
     * @param element A model class. For interface purposes, this must be an Object.
     * @return always false if its anything other than the current table. True if [com.raizlabs.android.dbflow.structure.Model.exists] passes.
     */
    override operator fun contains(element: T): Boolean {
        return internalCursorList.instanceAdapter.exists(element)
    }

    /**
     * If the collection is null or empty, we return false.
     *
     * @param elements The collection to check if all exist within the table.
     * @return true if all items exist in table, false if at least one fails.
     */
    override fun containsAll(elements: Collection<T>): Boolean {
        var contains = !elements.isEmpty()
        if (contains) {
            contains = elements.all { it in this }
        }
        return contains
    }

    override fun get(position: Long): T {
        return internalCursorList.get(position)
    }

    override fun cursor(): Cursor? {
        return internalCursorList.cursor()
    }

    /**
     * Returns the item from the backing [FlowCursorList]. First call
     * will load the model from the cursor, while subsequent calls will use the cache.
     *
     * @param index the row from the internal [FlowCursorList] query that we use.
     * @return A model converted from the internal [FlowCursorList]. For
     * performance improvements, ensure caching is turned on.
     */
    override operator fun get(index: Int): T = internalCursorList.get(index.toLong())

    override fun indexOf(element: T): Int {
        throw UnsupportedOperationException(
                "We cannot determine which index in the table this item exists at efficiently")
    }

    override fun isEmpty(): Boolean {
        return internalCursorList.isEmpty
    }

    /**
     * @return An iterator from [FlowCursorList.getAll].
     * Be careful as this method will convert all data under this table into a list of [T] in the UI thread.
     */
    override fun iterator(): FlowCursorIterator<T> {
        return FlowCursorIterator(this)
    }

    override fun iterator(startingLocation: Int, limit: Long): FlowCursorIterator<T> {
        return FlowCursorIterator(this, startingLocation, limit)
    }

    override fun lastIndexOf(element: T): Int {
        throw UnsupportedOperationException(
                "We cannot determine which index in the table this item exists at efficiently")
    }

    /**
     * @return A list iterator from the [FlowCursorList.getAll].
     * Be careful as this method will convert all data under this table into a list of [T] in the UI thread.
     */
    override fun listIterator(): ListIterator<T> {
        return FlowCursorIterator(this)
    }

    /**
     * @param location The index to start the iterator.
     * @return A list iterator from the [FlowCursorList.getAll].
     * Be careful as this method will convert all data under this table into a list of [T] in the UI thread.
     */
    override fun listIterator(location: Int): ListIterator<T> {
        return FlowCursorIterator(this, location)
    }

    override val size: Int
        get() = internalCursorList.count.toInt()

    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        val tableList = internalCursorList.all
        return tableList.subList(fromIndex, toIndex)
    }

    override fun close() {
        internalCursorList.close()
    }

    class Builder<T : Any> {

        internal val table: Class<T>

        internal var transact: Boolean = false
        internal var changeInTransaction: Boolean = false
        internal var cursor: Cursor? = null
        internal var modelQueriable: ModelQueriable<T>

        internal constructor(cursorList: FlowCursorList<T>) {
            table = cursorList.table
            cursor = cursorList.cursor()
            modelQueriable = cursorList.modelQueriable
        }

        constructor(modelQueriable: ModelQueriable<T>) {
            this.table = modelQueriable.table
            this.modelQueriable = modelQueriable
        }

        fun cursor(cursor: Cursor) = apply {
            this.cursor = cursor
        }

        fun transact(transact: Boolean) = apply {
            this.transact = transact
        }

        /**
         * If true, when an operation occurs whenever we call endTransactionAndNotify, we refresh content.
         */
        fun changeInTransaction(changeInTransaction: Boolean) = apply {
            this.changeInTransaction = changeInTransaction
        }

        fun build() = FlowQueryList(this)
    }

    companion object {

        private val REFRESH_HANDLER = Handler(Looper.myLooper())
    }


}
