package com.raizlabs.android.dbflow.structure.database.transaction;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager;

/**
 * Description: This class manages batch database interactions. It is useful for retrieving, updating, saving,
 * and deleting lists of items. The bulk of DB operations should exist in this class.
 */
public class DefaultTransactionManager extends BaseTransactionManager {

    public DefaultTransactionManager(DatabaseDefinition databaseDefinition) {
        super(new DefaultTransactionQueue("DBFlow Transaction Queue"), databaseDefinition);
    }

    public DefaultTransactionManager(ITransactionQueue transactionQueue, DatabaseDefinition databaseDefinition) {
        super(transactionQueue, databaseDefinition);
    }

}
