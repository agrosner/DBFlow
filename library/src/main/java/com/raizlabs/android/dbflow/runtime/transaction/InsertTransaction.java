package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Creates an insert transaction for inserting data into the DB
 */
public class InsertTransaction<ModelClass extends Model> extends QueryTransaction<ModelClass> {


    /**
     * Constructs this INSERT with the specified column and values for the insert statement.
     *
     * @param dbTransactionInfo Information on how to process the request
     * @param insertTable       The table to insert into
     * @param columnValues      The column and their corresponding values.
     */
    public InsertTransaction(DBTransactionInfo dbTransactionInfo, Class<ModelClass> insertTable, Condition... columnValues) {
        super(dbTransactionInfo, new Insert<>(insertTable).columnValues(columnValues), null);
    }

    /**
     * Constructs this INSERT with the specified insert statement.
     *
     * @param dbTransactionInfo Information on how to process the request
     * @param insert            The insert statement to use.
     */
    public InsertTransaction(DBTransactionInfo dbTransactionInfo, Insert<ModelClass> insert) {
        super(dbTransactionInfo, insert);
    }

}
