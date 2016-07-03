package com.raizlabs.android.dbflow.list;


import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * Utility class to be added to DBFlow.
 *
 * @param <TModel>
 */
public class FlowSimpleCursorAdapter <TModel extends Model> extends SimpleCursorAdapter
{
    private final Class<TModel> modelClass_;
    private final ModelAdapter<TModel> modelAdapter_;

    @TargetApi (11)
    public FlowSimpleCursorAdapter (Context context, Class<TModel> modelClass, int layout, Cursor c, String[] from, int[] to, int flags)
    {
        super (context, layout, c, from, to, flags);

        this.modelClass_ = modelClass;
        this.modelAdapter_ = FlowManager.getModelAdapter (modelClass);
    }

    @Override
    public TModel getItem (int position)
    {
        Cursor cursor = (Cursor) super.getItem (position);
        return cursor != null ? this.modelAdapter_.loadFromCursor (cursor) : null;
    }
}
