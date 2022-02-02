package com.dbflow5.adapter

import com.dbflow5.annotation.Query

/**
 * Description: The baseclass for adapters to [Query] that defines how it interacts with the DB. The
 * where query is not defined here, rather its determined by the cursor used.
 */
@Deprecated(
    replaceWith = ReplaceWith("RetrievalAdapter<T>", "com.dbflow5.adapter"),
    message = "QueryModelAdapter is now redundant. Use Retrieval Adapter"
)
abstract class QueryModelAdapter<T : Any> :
    RetrievalAdapter<T>()
