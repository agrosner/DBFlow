package com.raizlabs.android.dbflow.runtime;

import android.content.ContentResolver;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * The default use case, it notifies via the {@link ContentResolver} system.
 */
public class ContentResolverNotifier implements ModelNotifier {
    @Override
    public <T> void notifyModelChanged(T model, ModelAdapter<T> adapter, BaseModel.Action action) {
        FlowManager.getContext().getContentResolver()
            .notifyChange(SqlUtils.getNotificationUri(adapter.getModelClass(), action,
                adapter.getPrimaryConditionClause(model).getConditions()), null, true);
    }
}
