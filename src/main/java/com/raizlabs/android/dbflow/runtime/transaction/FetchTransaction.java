package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.From;
import com.raizlabs.android.dbflow.sql.Select;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Runs a fetch on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
 */
public class FetchTransaction<ModelClass extends Model> extends BaseResultTransaction<ModelClass> {

    private From mFrom;

    /**
     * Creates an instance of this classs with defaulted {@link com.raizlabs.android.dbflow.sql.Select} all.
     * @param tableClass
     * @param resultReceiver
     */
    public FetchTransaction(Class<ModelClass> tableClass, ResultReceiver<List<ModelClass>> resultReceiver) {
        this(tableClass, new Select(), resultReceiver);
    }

    /**
     * Creates this class with the specified arguments.
     * @param tableClass The class we will retrieve the models from
     * @param select The select statement we will use to retrieve them.
     * @param resultReceiver The result we get.
     */
    public FetchTransaction(Class<ModelClass> tableClass, Select select, ResultReceiver<List<ModelClass>> resultReceiver) {
        this(select.from(tableClass), resultReceiver);
    }

    /**
     * Creates this class with a {@link com.raizlabs.android.dbflow.sql.From}
     * @param from The completed Sql Statement we will use to fetch the models
     * @param resultReceiver
     */
    public FetchTransaction(From from, ResultReceiver<List<ModelClass>> resultReceiver) {
        super(DBTransactionInfo.createFetch(), resultReceiver);
        mFrom = from;
    }

    @Override
    public boolean onReady() {
        return mFrom != null;
    }

    @Override
    public List<ModelClass> onExecute() {
        return mFrom.queryList();
    }

}
