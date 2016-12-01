package com.raizlabs.android.dbflow.single;

import android.annotation.TargetApi;
import android.content.Context;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;

/**
 * Load a single DBFlow model from a QueryModel.
 *
 * @param <TQueryModel>
 */
@TargetApi(11)
public class FlowQueryModelLoader <TQueryModel extends BaseQueryModel>
    extends FlowSingleModelLoader<TQueryModel> {

    public FlowQueryModelLoader (Context context, Class<TQueryModel> model, Queriable queriable) {
        super (context, model, FlowManager.getQueryModelAdapter (model), queriable);

        this.setObserveModel (false);
    }
}