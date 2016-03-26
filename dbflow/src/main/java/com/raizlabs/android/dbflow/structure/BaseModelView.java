package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.annotation.ModelViewQuery;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Description: Provides a base implementation for a ModelView. Use a {@link ModelView}
 * annotation to register it properly. Also you need to specify a singular
 * field via {@link ModelViewQuery}.
 */
public abstract class BaseModelView<TModel extends Model> extends NoModificationModel {

    private transient ModelViewAdapter<? extends Model, BaseModelView<TModel>> adapter;

    @Override
    public boolean exists() {
        return getModelViewAdapter().exists(this);
    }

    @SuppressWarnings("unchecked")
    public ModelViewAdapter<? extends Model, BaseModelView<TModel>> getModelViewAdapter() {
        if (adapter == null) {
            adapter = ((ModelViewAdapter<? extends Model, BaseModelView<TModel>>)
                    FlowManager.getModelViewAdapter(getClass()));
        }
        return adapter;
    }

}
