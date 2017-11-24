package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Provides a base class for objects that represent [QueryModel].
 */
@Deprecated("No subclass needed. Use extension methods instead.")
class BaseQueryModel : NoModificationModel() {

    override fun exists(wrapper: DatabaseWrapper): Boolean {
        throw NoModificationModel.InvalidSqlViewOperationException("Query " + wrapper.javaClass.name +
                " does not exist as a table." + "It's a convenient representation of a complex SQLite query.")
    }
}
