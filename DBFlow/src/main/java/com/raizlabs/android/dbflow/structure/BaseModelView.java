package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Description: Provides a base implementation for a ModelView. Use a {@link com.raizlabs.android.dbflow.annotation.ModelView}
 * annotation to register it properly.
 */
public abstract class BaseModelView<ModelClass extends Model> extends BaseFinalModel {

    private ModelViewAdapter<? extends Model, BaseModelView<ModelClass>> adapter;

    @Override
    public boolean exists() {
        return getModelViewAdapter().exists(this);
    }

    @SuppressWarnings("unchecked")
    public ModelViewAdapter<? extends Model, BaseModelView<ModelClass>> getModelViewAdapter() {
        if (adapter == null) {
            adapter = ((ModelViewAdapter<? extends Model, BaseModelView<ModelClass>>)
                    FlowManager.getModelViewAdapter(getClass()));
        }
        return adapter;
    }

}
