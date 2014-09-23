package com.grosner.dbflow.runtime.transaction;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.sql.From;
import com.grosner.dbflow.sql.Select;
import com.grosner.dbflow.sql.Where;
import com.grosner.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Runs a fetch on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
 */
public class SelectListTransaction<ModelClass extends Model> extends BaseResultTransaction<List<ModelClass>> {

    private Where<ModelClass> mWhere;

    /**
     * Creates an instance of this classs with defaulted {@link com.grosner.dbflow.sql.Select} all.
     *
     * @param tableClass
     * @param resultReceiver
     */
    public SelectListTransaction(FlowManager flowManager, Class<ModelClass> tableClass, ResultReceiver<List<ModelClass>> resultReceiver) {
        this(new Select(flowManager).from(tableClass), resultReceiver);
    }

    /**
     * Creates this class with the specified arguments.
     *
     * @param from           The from to use
     * @param resultReceiver The result we get.
     */
    public SelectListTransaction(From<ModelClass> from, ResultReceiver<List<ModelClass>> resultReceiver) {
        this(from.where(), resultReceiver);
    }

    /**
     * Creates this class with a {@link com.grosner.dbflow.sql.From}
     *
     * @param where          The completed Sql Statement we will use to fetch the models
     * @param resultReceiver
     */
    public SelectListTransaction(Where<ModelClass> where, ResultReceiver<List<ModelClass>> resultReceiver) {
        super(DBTransactionInfo.createFetch(), resultReceiver);
        mWhere = where;
    }

    @Override
    public boolean onReady() {
        return mWhere != null;
    }

    @Override
    public List<ModelClass> onExecute() {
        return mWhere.queryList();
    }

}
