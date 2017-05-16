package com.raizlabs.android.dbflow.structure;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: The baseclass for adapters to {@link QueryModel} that defines how it interacts with the DB. The
 * where query is not defined here, rather its determined by the query used.
 */
public abstract class QueryModelAdapter<TQueryModel> extends
        InstanceAdapter<TQueryModel> {

    public QueryModelAdapter(DatabaseDefinition databaseDefinition) {
        super(databaseDefinition);
    }

    @Override
    public OperatorGroup getPrimaryConditionClause(@NonNull TQueryModel model) {
        throw new UnsupportedOperationException("QueryModels cannot check for existence");
    }

    @Override
    public boolean exists(@NonNull TQueryModel model) {
        throw new UnsupportedOperationException("QueryModels cannot check for existence");
    }

    @Override
    public boolean exists(@NonNull TQueryModel model, @NonNull DatabaseWrapper databaseWrapper) {
        throw new UnsupportedOperationException("QueryModels cannot check for existence");
    }
}
