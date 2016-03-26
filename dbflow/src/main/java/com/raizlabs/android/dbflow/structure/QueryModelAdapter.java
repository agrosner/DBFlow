package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: The baseclass for adapters to {@link QueryModel} that defines how it interacts with the DB. The
 * where query is not defined here, rather its determined by the query used.
 */
public abstract class QueryModelAdapter<TQueryModel extends BaseQueryModel> extends
        InstanceAdapter<TQueryModel, TQueryModel> {

    @Override
    public ConditionGroup getPrimaryConditionClause(TQueryModel model) {
        throw new UnsupportedOperationException("QueryModels cannot check for existence");
    }

    @Override
    public boolean exists(TQueryModel model) {
        throw new UnsupportedOperationException("QueryModels cannot check for existence");
    }

    @Override
    public boolean exists(TQueryModel model, DatabaseWrapper databaseWrapper) {
        throw new UnsupportedOperationException("QueryModels cannot check for existence");
    }
}
