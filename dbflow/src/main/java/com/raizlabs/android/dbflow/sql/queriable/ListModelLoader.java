package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Loads a {@link List} of {@link TModel}.
 */
public class ListModelLoader<TModel extends Model> extends ModelLoader<TModel, List<TModel>> {

    public ListModelLoader(Class<TModel> modelClass) {
        super(modelClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<TModel> convertToData(@NonNull Cursor cursor) {
        final List<TModel> modelList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                TModel model = (TModel) getInstanceAdapter().newInstance();
                getInstanceAdapter().loadFromCursor(cursor, model);
                modelList.add(model);
            } while (cursor.moveToNext());
        }
        return modelList;
    }
}
