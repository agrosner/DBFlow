package com.raizlabs.dbflow5.query.list

import com.raizlabs.dbflow5.AutoCloseable
import com.raizlabs.dbflow5.JvmOverloads
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.ModelQueriable
import com.raizlabs.dbflow5.query.Transformable
import com.raizlabs.dbflow5.query.constrain

/**
 * Description: Provides iteration capabilities to a [FlowCursorList].
 */
class FlowCursorIterator<T : Any>
@JvmOverloads constructor(
    databaseWrapper: DatabaseWrapper,
    cursorList: IFlowCursorIterator<T>,
    startingLocation: Long,
    private var count: Long = cursorList.count - startingLocation)
    : ListIterator<T>, AutoCloseable {
    private var reverseIndex: Long = 0
    private var startingCount: Long = 0
    private val cursorList: IFlowCursorIterator<T>

    constructor(databaseWrapper: DatabaseWrapper,
                cursorList: IFlowCursorIterator<T>) : this(databaseWrapper, cursorList, 0, cursorList.count)

    init {
        var newCursorList = cursorList
        var newStartingLocation = startingLocation
        if (!cursorList.trackingCursor) {
            // no cursor specified, we can optimize the query to return results within SQL range, rather than all rows.
            @Suppress("UNCHECKED_CAST")
            val _cursorList = when (cursorList) {
                is FlowCursorList<*> -> cursorList as FlowCursorList<T>
                is FlowQueryList<*> -> cursorList.internalCursorList as FlowCursorList<T>
                else -> throw IllegalArgumentException("The specified ${IFlowCursorIterator::class.java.simpleName} " +
                    "must track cursor unless it is a FlowCursorList or FlowQueryList")
            }
            val modelQueriable = _cursorList.modelQueriable
            if (modelQueriable is Transformable<*>) {
                @Suppress("UNCHECKED_CAST")
                newCursorList = (modelQueriable as Transformable<T>)
                    .constrain(startingLocation, count)
                    .cursorList(databaseWrapper)
                this.count = newCursorList.count
                newStartingLocation = 0
            }
        }
        newCursorList.cursor?.let { cursor ->
            // request larger than actual count. Can almost never be long, but we keep precision.
            if (this.count > cursor.count - newStartingLocation) {
                this.count = cursor.count - newStartingLocation
            }

            cursor.moveToPosition(newStartingLocation.toInt() - 1)
            startingCount = newCursorList.count
            reverseIndex = this.count
            reverseIndex -= newStartingLocation

            if (reverseIndex < 0) {
                reverseIndex = 0
            }
        }
        this.cursorList = newCursorList
    }

    val isClosed
        get() = cursorList.isClosed

    override fun close() {
        cursorList.close()
    }

    override fun hasNext(): Boolean {
        checkSizes()
        return reverseIndex > 0
    }

    override fun hasPrevious(): Boolean {
        checkSizes()
        return reverseIndex < count
    }

    override fun next(): T {
        checkSizes()
        val item = cursorList[count - reverseIndex]
        reverseIndex--
        return item
    }

    override fun nextIndex(): Int = (reverseIndex + 1).toInt()

    override fun previous(): T {
        checkSizes()
        val item = cursorList[count - reverseIndex]
        reverseIndex++
        return item
    }

    override fun previousIndex(): Int = reverseIndex.toInt()

    private fun checkSizes() {
        if (startingCount != cursorList.count) {
            throw RuntimeException("Concurrent Modification: Cannot change Cursor data " +
                "during iteration. Expected $startingCount, found: ${cursorList.count}")
        }
    }

    private fun getQueriableFromParams(transformable: Transformable<T>,
                                       startPosition: Long, max: Long): ModelQueriable<T> {
        var tr: Transformable<T> = transformable
        @Suppress("UNCHECKED_CAST")
        if (tr is QueryCloneable<*>) {
            tr = tr.cloneSelf() as Transformable<T>
        }
        return tr.offset(startPosition).limit(max)
    }
}
