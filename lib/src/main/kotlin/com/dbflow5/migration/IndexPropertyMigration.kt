package com.dbflow5.migration

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.property.IndexProperty

/**
 * Description: Allows you to specify if and when an [IndexProperty] gets used or created.
 */
abstract class IndexPropertyMigration(
    /**
     * @return true if create the index, false to drop the index.
     */
    open val shouldCreate: Boolean = true) : BaseMigration() {

    abstract val indexProperty: IndexProperty<*>

    override suspend fun migrate(database: DatabaseWrapper) {
        if (shouldCreate) {
            indexProperty.createIfNotExists(database)
        } else {
            indexProperty.drop(database)
        }
    }
}
