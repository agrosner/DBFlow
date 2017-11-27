package com.raizlabs.android.dbflow.transaction

import com.raizlabs.android.dbflow.config.DatabaseDefinition

/**
 * Description: This class manages batch database interactions. It is useful for retrieving, updating, saving,
 * and deleting lists of items. The bulk of DB operations should exist in this class.
 */
class DefaultTransactionManager : BaseTransactionManager {

    constructor(databaseDefinition: DatabaseDefinition)
            : super(DefaultTransactionQueue("DBFlow Transaction Queue"), databaseDefinition)

    constructor(transactionQueue: ITransactionQueue,
                databaseDefinition: DatabaseDefinition)
            : super(transactionQueue, databaseDefinition)

}
