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

    @NonNull
    private final String contentAuthority;

    public ContentResolverNotifier(@NonNull String contentAuthority) {
        this.contentAuthority = contentAuthority;
    }

    @Override
    public <T> void notifyModelChanged(@NonNull T model, @NonNull ModelAdapter<T> adapter,
                                       @NonNull BaseModel.Action action) {
        if (FlowContentObserver.shouldNotify()) {
            FlowManager.getContext().getContentResolver()
                    .notifyChange(SqlUtils.getNotificationUri(contentAuthority,
                            adapter.getModelClass(), action,
                            adapter.getPrimaryConditionClause(model).getConditions()), null, true);
        }
    }

    @Override
    public <T> void notifyTableChanged(@NonNull Class<T> table, @NonNull BaseModel.Action action) {
        if (FlowContentObserver.shouldNotify()) {
            FlowManager.getContext().getContentResolver()
                    .notifyChange(SqlUtils.getNotificationUri(contentAuthority,
                            table, action, (SQLOperator[]) null), null, true);
        }
    }

    @Override
    public TableNotifierRegister newRegister() {
        return new FlowContentTableNotifierRegister(contentAuthority);
    }

    public static class FlowContentTableNotifierRegister implements TableNotifierRegister {
        private final FlowContentObserver flowContentObserver;

        @Nullable
        private OnTableChangedListener tableChangedListener;

        public FlowContentTableNotifierRegister(@NonNull String contentAuthority) {
            flowContentObserver = new FlowContentObserver(contentAuthority);
            flowContentObserver.addOnTableChangedListener(internalContentChangeListener);
        }

        @Override
        public <T> void register(@NonNull Class<T> tClass) {
            flowContentObserver.registerForContentChanges(FlowManager.getContext(), tClass);
        }

        @Override
        public <T> void unregister(@NonNull Class<T> tClass) {
            flowContentObserver.unregisterForContentChanges(FlowManager.getContext());
        }

        @Override
        public void unregisterAll() {
            flowContentObserver.removeTableChangedListener(internalContentChangeListener);
            this.tableChangedListener = null;
        }

        @Override
        public void setListener(@Nullable OnTableChangedListener contentChangeListener) {
            this.tableChangedListener = contentChangeListener;
        }

        @Override
        public boolean isSubscribed() {
            return !flowContentObserver.isSubscribed();
        }

        private final OnTableChangedListener internalContentChangeListener
                = new OnTableChangedListener() {

            @Override
            public void onTableChanged(@Nullable Class<?> tableChanged, @NonNull BaseModel.Action action) {
                if (tableChangedListener != null) {
                    tableChangedListener.onTableChanged(tableChanged, action);
                }
            }
        };
    }
}
