package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;

/**
 * Description: The baseclass for adapters to {@link QueryModel} that defines how it interacts with the DB. The
 * where query is not defined here, rather its determined by the query used.
 */
public abstract class QueryModelAdapter<ModelClass extends Model> implements InstanceAdapter<ModelClass, ModelClass> {

    @Override
    public ConditionQueryBuilder<ModelClass> getPrimaryModelWhere(ModelClass model) {
        throw new UnsupportedOperationException("QueryModels cannot check for existence");
    }

    @Override
    public boolean exists(ModelClass model) {
        throw new UnsupportedOperationException("QueryModels cannot check for existence");
    }
}
