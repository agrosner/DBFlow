package com.grosner.dbflow.structure;

import android.database.Cursor;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public abstract class ModelViewAdapter<ModelClass extends Model, ModelViewClass extends BaseModelView<ModelClass>> {

    public abstract ModelViewClass loadFromCursor(Cursor cursor);

    public abstract String getCreationQuery();

    public abstract String getPrimaryModelWhere(ModelViewClass modelView);

    public abstract boolean exists(ModelViewClass model);

    public abstract String getViewName();
}
