
package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.container.ModelContainer;
import com.raizlabs.android.dbflow.structure.container.ModelContainerAdapter;

/**
 * Description: loads data into a {@link ModelContainer}. Only works when the passed {@link ModelContainer} is non-null,
 * since we don't know the type of object to create or return.
 */
public class ModelContainerLoader<TModel extends Model> extends ModelLoader<TModel, ModelContainer<TModel, ?>> {

    private ModelContainerAdapter<TModel> modelContainerAdapter;

    public ModelContainerLoader(Class<TModel> modelClass) {
        super(modelClass);
        modelContainerAdapter = FlowManager.getContainerAdapter(modelClass);
    }

    @Override
    protected ModelContainer<TModel, ?> convertToData(@NonNull Cursor cursor, @Nullable ModelContainer<TModel, ?> data) {
        if (data != null) {
            if (cursor.moveToFirst()) {
                modelContainerAdapter.loadFromCursor(cursor, data);
            }
            return data;
        } else {
            return null;
        }

    }


}
