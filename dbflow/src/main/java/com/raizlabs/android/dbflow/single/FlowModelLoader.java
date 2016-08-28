package com.raizlabs.android.dbflow.single;

import android.annotation.TargetApi;
import android.content.Context;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Load a single model from the database.
 *
 * @param <TModel>
 */
@TargetApi(11)
public class FlowModelLoader <TModel extends Model>
    extends FlowSingleModelLoader<TModel, TModel> {

    public FlowModelLoader (Context context, Class<TModel> model, Queriable queriable) {
        super (context, model, FlowManager.getModelAdapter (model), queriable);
    }
}