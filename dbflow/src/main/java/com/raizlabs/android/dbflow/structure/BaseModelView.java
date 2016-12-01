package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.annotation.ModelViewQuery;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Description: Provides a base implementation for a ModelView. Use a {@link ModelView}
 * annotation to register it properly. Also you need to specify a singular
 * field via {@link ModelViewQuery}.
 */
public abstract class BaseModelView extends NoModificationModel {

    private transient RetrievalAdapter adapter;

    @SuppressWarnings("unchecked")
    @Override
    public RetrievalAdapter getRetrievalAdapter() {
        if (adapter == null) {
            adapter = FlowManager.getModelViewAdapter(getClass());
        }
        return adapter;
    }

}
