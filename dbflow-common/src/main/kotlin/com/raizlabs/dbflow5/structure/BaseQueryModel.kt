package com.raizlabs.dbflow5.structure

import com.raizlabs.dbflow5.annotation.QueryModel
import com.raizlabs.dbflow5.database.DatabaseWrapper

/**
 * Description: Provides a base class for objects that represent [QueryModel].
 */
class BaseQueryModel : NoModificationModel() {

    override fun exists(wrapper: DatabaseWrapper): Boolean {
        throw InvalidSqlViewOperationException("Query ${wrapper::class} does not exist as a table." +
            "It's a convenient representation of a complex SQLite cursor.")
    }
}
