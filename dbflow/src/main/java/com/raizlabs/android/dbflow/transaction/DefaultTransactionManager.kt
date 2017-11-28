package com.raizlabs.android.dbflow.transaction

import com.raizlabs.android.dbflow.config.DatabaseDefinition

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
