package com.grosner.dbflow.structure;

import android.database.Cursor;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface ModelAdapter<ModelClass extends Model> {

    public ModelClass loadFromCursor(Cursor cursor);

    public void save(boolean async, ModelClass model, int saveMode);

    public boolean exists(ModelClass model);

    public String getPrimaryModelWhere(ModelClass model);

    public Class<ModelClass> getModelClass();
}
