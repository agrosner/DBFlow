package com.raizlabs.android.dbflow.sql.queriable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.FlowCursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Loads a {@link List} of {@link TModel}.
 */
@SuppressWarnings("ConstantConditions")
public class ListModelLoader<TModel> extends ModelLoader<TModel, List<TModel>> {

    public ListModelLoader(@NonNull Class<TModel> modelClass) {
        super(modelClass);
    }

    @NonNull
    @Override
    public List<TModel> load(String query) {
        return super.load(query);
    }

    @NonNull
    @Override
    public List<TModel> load(String query, @Nullable List<TModel> data) {
        return super.load(query, data);
    }

    @NonNull
    @Override
    public List<TModel> load(@NonNull DatabaseWrapper databaseWrapper, String query) {
        return super.load(databaseWrapper, query);
    }

    @NonNull
    @Override
    public List<TModel> load(@NonNull DatabaseWrapper databaseWrapper, String query,
                             @Nullable List<TModel> data) {
        return super.load(databaseWrapper, query, data);
    }

    @NonNull
    @Override
    public List<TModel> load(@Nullable FlowCursor cursor) {
        return super.load(cursor);
    }

    @NonNull
    @Override
    public List<TModel> load(@Nullable FlowCursor cursor, @Nullable List<TModel> data) {
        if (data == null) {
            data = new ArrayList<>();
        } else {
            data.clear();
        }
        return super.load(cursor, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    public List<TModel> convertToData(@NonNull FlowCursor cursor, @Nullable List<TModel> data) {
        if (data == null) {
            data = new ArrayList<>();
        } else {
            data.clear();
        }

        if (cursor.moveToFirst()) {
            do {
                TModel model = getInstanceAdapter().newInstance();
                getInstanceAdapter().loadFromCursor(cursor, model);
                data.add(model);
            } while (cursor.moveToNext());
        }
        return data;
    }
}
