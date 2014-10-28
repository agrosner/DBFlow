package com.grosner.dbflow.structure;

import android.database.Cursor;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface ModelAdapter<ModelClass extends Model> {

    public void loadFromCursor(Cursor cursor);

    public void save(boolean async, ModelClass model, int saveMode);

    public String getPrimaryModelWhere(ModelClass model);
}
