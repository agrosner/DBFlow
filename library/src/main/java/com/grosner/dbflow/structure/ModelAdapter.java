package com.grosner.dbflow.structure;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface ModelAdapter<ModelClass extends Model> {

    public void loadFromCursor(Cursor cursor);

    public void save(ModelClass model, int saveMode);
}
