package com.raizlabs.dbflow5.transaction

import com.raizlabs.dbflow5.database.DBFlowDatabase

/**
 * Description: This class manages batch database interactions. Places DB operations onto the same Thread.
 */
class DefaultTransactionManager : BaseTransactionManager {

    constructor(databaseDefinition: DBFlowDatabase)
        : super(DefaultTransactionQueue("DBFlow Transaction Queue"), databaseDefinition)

    constructor(transactionQueue: ITransactionQueue,
                databaseDefinition: DBFlowDatabase)
        : super(transactionQueue, databaseDefinition)

}
