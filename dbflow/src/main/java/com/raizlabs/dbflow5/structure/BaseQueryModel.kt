package com.raizlabs.dbflow5.structure

import com.raizlabs.dbflow5.annotation.QueryModel
import com.raizlabs.dbflow5.database.DatabaseWrapper

/**
 * Description: Provides a base class for objects that represent [QueryModel].
 */
@Deprecated("No subclass needed. Use extension methods instead.")
class BaseQueryModel : NoModificationModel() {

    override fun exists(wrapper: DatabaseWrapper): Boolean {
        throw InvalidSqlViewOperationException("Query " + wrapper.javaClass.name +
                " does not exist as a table." + "It's a convenient representation of a complex SQLite query.")
    }
}
