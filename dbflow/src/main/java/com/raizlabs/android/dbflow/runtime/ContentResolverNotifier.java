package com.raizlabs.android.dbflow.runtime;

import android.content.ContentResolver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.SQLOperator;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * The default use case, it notifies via the {@link ContentResolver} system.
 */
public class ContentResolverNotifier implements ModelNotifier {
    @Override
    public <T> void notifyModelChanged(@Nullable T model, @NonNull ModelAdapter<T> adapter,
                                       @NonNull BaseModel.Action action) {
        if (FlowContentObserver.shouldNotify()) {
            FlowManager.getContext().getContentResolver()
                .notifyChange(SqlUtils.getNotificationUri(adapter.getModelClass(), action,
                    adapter.getPrimaryConditionClause(model).getConditions()), null, true);
        }
    }

    @Override
    public <T> void notifyTableChanged(@NonNull Class<T> table, @NonNull BaseModel.Action action) {
        if (FlowContentObserver.shouldNotify()) {
            FlowManager.getContext().getContentResolver()
                .notifyChange(SqlUtils.getNotificationUri(table, action, (SQLOperator[]) null), null, true);
        }
    }

}
