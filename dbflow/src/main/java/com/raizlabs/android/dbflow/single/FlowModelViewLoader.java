package com.raizlabs.android.dbflow.single;

import android.annotation.TargetApi;
import android.content.Context;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Utility class to be added to DBFlow.
 *
 * @param <TModel>
 */
@TargetApi(11)
public class FlowModelViewLoader <TModel extends Model, TModelView extends BaseModelView<TModel>>
  extends FlowSingleModelLoader<TModel, TModelView>
{
  public FlowModelViewLoader (Context context,
                              Class <TModel> model,
                              Class <TModelView> modelView,
                              Queriable queriable)
  {
    super (context, model, (InstanceAdapter<TModel, TModelView>) FlowManager.getModelViewAdapter (modelView), queriable);
  }
}