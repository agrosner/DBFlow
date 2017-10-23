package com.raizlabs.android.dbflow.sql.migration

import com.raizlabs.android.dbflow.sql.language.property.IndexProperty
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Allows you to specify if and when an [IndexProperty] gets used or created.
 */
abstract class IndexPropertyMigration(
        /**
         * @return true if create the index, false to drop the index.
         */
        open val shouldCreate: Boolean = true) : BaseMigration() {

    abstract val indexProperty: IndexProperty<*>

    override fun migrate(database: DatabaseWrapper) {
        if (shouldCreate) {
            indexProperty.createIfNotExists(database)
        } else {
            indexProperty.drop(database)
        }
    }
}
