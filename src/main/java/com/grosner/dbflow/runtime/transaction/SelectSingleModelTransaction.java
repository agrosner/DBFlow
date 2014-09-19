package com.grosner.dbflow.runtime.transaction;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.sql.Select;
import com.grosner.dbflow.sql.Where;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Runs a fetch on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}, returning only the first item.
 */
public class SelectSingleModelTransaction<ModelClass extends Model> extends BaseResultTransaction<ModelClass> {

    private Where<ModelClass> mFrom;

    public SelectSingleModelTransaction(FlowManager flowManager, Class<ModelClass> tableClass, ResultReceiver<ModelClass> resultReceiver) {
        this(tableClass, new Select(flowManager), resultReceiver);
    }

    /**
     * Creates this class with the specified arguments.
     *
     * @param tableClass     The class we will retrieve the models from
     * @param select         The select statement we will use to retrieve them.
     * @param resultReceiver The result we get.
     */
    public SelectSingleModelTransaction(Class<ModelClass> tableClass, Select select, ResultReceiver<ModelClass> resultReceiver) {
        this(select.from(tableClass).where(), resultReceiver);
    }

    /**
     * Creates this class with a {@link com.grosner.dbflow.sql.From}
     *
     * @param from           The completed Sql Statement we will use to fetch the models
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
