package com.raizlabs.android.dbflow.structure.container;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.Map;

public class ExternalForeignKeyContainer<ModelClass extends Model> extends ForeignKeyContainer<ModelClass> {
    private final FlowContentObserver observer = new FlowContentObserver();

    private final FlowContentObserver.OnModelStateChangedListener onModelStateChangedListener = new FlowContentObserver.OnModelStateChangedListener () {
        @Override
        public void onModelStateChanged(Class<? extends Model> table, BaseModel.Action action, SQLCondition[] primaryKeyValues) {
        // TODO Delete rows from the table referencing this table.
        }
    };

    public ExternalForeignKeyContainer(ModelContainer<ModelClass, ?> existingContainer) {
        super(existingContainer);
        initializeObserver();
    }

    public ExternalForeignKeyContainer(Class<ModelClass> table) {
        super(table);
        initializeObserver();
    }

    public ExternalForeignKeyContainer(Class<ModelClass> table, Map<String, Object> data) {
        super(table, data);
        initializeObserver();
    }

    private void initializeObserver() {
        observer.registerForContentChanges(FlowManager.getContext(), getTable());
        observer.addModelChangeListener(onModelStateChangedListener);
    }

    public void close() {
        observer.unregisterForContentChanges(FlowManager.getContext());
    }
}
