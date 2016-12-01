package com.raizlabs.android.dbflow.single;

import android.annotation.TargetApi;
import android.content.Context;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Load a single DBFlow model from a ViewModel.
 *
 * @param <TModel>
 */
@TargetApi(11)
public class FlowModelViewLoader <TModel extends Model, TModelView extends BaseModelView>
    extends FlowSingleModelLoader<TModel> {

    @SuppressWarnings("unchecked")
    public FlowModelViewLoader(Context context, Class<TModel> model, Class<TModelView> modelView, Queriable queriable) {
        super(context, model, (InstanceAdapter<TModel>) FlowManager.getModelViewAdapter(modelView), queriable);
    }
}