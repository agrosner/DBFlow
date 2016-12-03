package com.raizlabs.android.dbflow.list;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.widget.CursorAdapter;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * Specialization of CursorAdapter for DBFLow models. The getItem() method
 * returns a model element instead of a Cursor object.
 *
 * @param <TModel>
 */
public abstract class FlowCursorAdapter <TModel extends Model> extends CursorAdapter {
    private final ModelAdapter<TModel> modelAdapter;

    public FlowCursorAdapter(Context context, Class<TModel> modelClass, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);

        this.modelAdapter = FlowManager.getModelAdapter(modelClass);
    }

    @TargetApi(11)
    public FlowCursorAdapter(Context context, Class<TModel> modelClass, Cursor c, int flags) {
        super(context, c, flags);

        this.modelAdapter = FlowManager.getModelAdapter(modelClass);
    }

    @Override
    public TModel getItem(int position) {
        Cursor cursor = (Cursor) super.getItem(position);
        return cursor != null ? this.modelAdapter.loadFromCursor(cursor) : null;
    }
}
