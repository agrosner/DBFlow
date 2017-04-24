package com.raizlabs.android.dbflow.sql.queriable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.structure.database.FlowCursor;

/**
 * Description: Responsible for loading data into a single object.
 */
public class SingleModelLoader<TModel> extends ModelLoader<TModel, TModel> {

    public SingleModelLoader(Class<TModel> modelClass) {
        super(modelClass);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public TModel convertToData(@NonNull final FlowCursor cursor, @Nullable TModel data, boolean moveToFirst) {
        if (!moveToFirst || cursor.moveToFirst()) {
            if (data == null) {
                data = getInstanceAdapter().newInstance();
            }
            getInstanceAdapter().loadFromCursor(cursor, data);
        }
        return data;
    }

    @Override
    public TModel convertToData(@NonNull final FlowCursor cursor, @Nullable TModel data) {
        return convertToData(cursor, data, true);
    }
}
