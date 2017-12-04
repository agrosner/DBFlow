package com.raizlabs.dbflow5.transaction

import com.raizlabs.dbflow5.config.DatabaseDefinition

/**
 * Description: This class manages batch database interactions. Places DB operations onto the same Thread.
 */
class DefaultTransactionManager : BaseTransactionManager {

    constructor(databaseDefinition: DatabaseDefinition)
        : super(DefaultTransactionQueue("DBFlow Transaction Queue"), databaseDefinition)

    constructor(transactionQueue: ITransactionQueue,
                databaseDefinition: DatabaseDefinition)
        : super(transactionQueue, databaseDefinition)

}
