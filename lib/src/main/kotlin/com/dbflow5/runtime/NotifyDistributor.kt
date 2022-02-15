package com.dbflow5.runtime

import com.dbflow5.database.DatabaseWrapper

/**
 * Description: Distributes notifications to the [ModelNotifier].
 */
class NotifyDistributor
private constructor(
    override val db: DatabaseWrapper
) : ModelNotifier {

    override fun <Table : Any> onChange(notification: ModelNotification<Table>) {
        db.associatedDBFlowDatabase.getModelNotifier()
            .onChange(notification)
    }

    companion object {

        private val distributorMap = mutableMapOf<DatabaseWrapper, NotifyDistributor>()

        operator fun invoke(db: DatabaseWrapper): NotifyDistributor =
            distributorMap.getOrPut(db) {
                NotifyDistributor(db)
            }
    }
}
