package com.raizlabs.android.dbflow.structure.database.transaction

import com.raizlabs.android.dbflow.config.database
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import kotlinx.coroutines.experimental.async

/**
 * Performs database operations on the background in coroutines.
 */
inline suspend fun <reified T : Any> db(crossinline function: DatabaseWrapper.() -> Unit) {
    async {
        function(database<T>().writableDatabase)
    }
}