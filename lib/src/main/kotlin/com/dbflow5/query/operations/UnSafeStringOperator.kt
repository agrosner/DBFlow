package com.dbflow5.query.operations

/**
 * This operator uses a selection string and args to form a query.
 * Not recommended for normal queries, but can be used as a convenience.
 */
data class UnSafeStringOperator(
    private val selection: String,
    private val selectionArgs: List<String>,
) : Operator<String> {
    override val query: String by lazy {
        selectionArgs.fold(selection) { selection, arg ->
            selection.replaceFirst("\\?".toRegex(), arg)
        }
    }
}
