package com.raizlabs.android.dbflow.runtime;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * Description: Distributes notifications to the {@link ModelNotifier}.
 */
public class NotifyDistributor implements ModelNotifier {

    private static NotifyDistributor distributor;

    public static NotifyDistributor get() {
        if (distributor == null) {
            distributor = new NotifyDistributor();
        }
        return distributor;
    }

    @Override
    public <TModel> void notifyModelChanged(@Nullable TModel model,
                                            @NonNull ModelAdapter<TModel> modelAdapter,
                                            @NonNull BaseModel.Action action) {
        FlowManager.getDatabaseForTable(modelAdapter.getModelClass())
            .getModelNotifier().notifyModelChanged(model, modelAdapter, action);
    }

    /**
     * Notifies listeners of table-level changes from the SQLite-wrapper language.
     */
    @Override
    public <TModel> void notifyTableChanged(@NonNull Class<TModel> table,
                                            @NonNull BaseModel.Action action) {
        FlowManager.getDatabaseForTable(table)
            .getModelNotifier().notifyTableChanged(table, action);
    }
}
