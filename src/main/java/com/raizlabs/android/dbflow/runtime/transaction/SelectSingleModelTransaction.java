package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.Select;
import com.raizlabs.android.dbflow.sql.Where;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Runs a fetch on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}, returning only the first item.
 */
public class SelectSingleModelTransaction<ModelClass extends Model> extends BaseResultTransaction<ModelClass, ModelClass> {

    private Where<ModelClass> mFrom;

    public SelectSingleModelTransaction(Class<ModelClass> tableClass, ResultReceiver<ModelClass> resultReceiver) {
        this(tableClass, new Select(), resultReceiver);
    }

    /**
     * Creates this class with the specified arguments.
     * @param tableClass The class we will retrieve the models from
     * @param select The select statement we will use to retrieve them.
     * @param resultReceiver The result we get.
     */
    public SelectSingleModelTransaction(Class<ModelClass> tableClass, Select select, ResultReceiver<ModelClass> resultReceiver) {
        this(select.from(tableClass).where(), resultReceiver);
    }

    /**
     * Creates this class with a {@link com.raizlabs.android.dbflow.sql.From}
     * @param from The completed Sql Statement we will use to fetch the models
     * @param resultReceiver
     */
    public SelectSingleModelTransaction(Where<ModelClass> from, ResultReceiver<ModelClass> resultReceiver) {
        super(DBTransactionInfo.createFetch(), resultReceiver);
        mFrom = from;
    }


    @Override
    public ModelClass onExecute() {
        return mFrom.querySingle();
    }
}
