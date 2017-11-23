package com.raizlabs.android.dbflow.list

import java.util.*

/**
 * Description: Provides iteration capabilities to a [FlowCursorList].
 */
class FlowCursorIterator<TModel>
@JvmOverloads constructor(
        private val cursorList: IFlowCursorIterator<TModel>,
        startingLocation: Int,
        private var count: Long = cursorList.count - startingLocation)
    : ListIterator<TModel>, AutoCloseable {
    private var reverseIndex: Long = 0
    private var startingCount: Long = 0

    constructor(cursorList: IFlowCursorIterator<TModel>) : this(cursorList, 0, cursorList.count)

    init {
        cursorList.cursor()?.let { cursor ->
            // request larger than actual count.
            if (this.count > cursor.count - startingLocation) {
                this.count = (cursor.count - startingLocation).toLong()
            }

            cursor.moveToPosition(startingLocation - 1)
            startingCount = cursorList.count
            reverseIndex = this.count
            reverseIndex -= startingLocation.toLong()

            if (reverseIndex < 0) {
                reverseIndex = 0
            }
        }
    }

    @Throws(Exception::class)
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

    override fun next(): TModel {
        checkSizes()
        val item = cursorList.get(count - reverseIndex)
        reverseIndex--
        return item
    }

    override fun nextIndex(): Int = (reverseIndex + 1).toInt()

    override fun previous(): TModel {
        checkSizes()
        val item = cursorList.get(count - reverseIndex)
        reverseIndex++
        return item
    }

    override fun previousIndex(): Int = reverseIndex.toInt()

    private fun checkSizes() {
        if (startingCount != cursorList.count) {
            throw ConcurrentModificationException("Cannot change Cursor data during iteration.")
        }
    }
}
