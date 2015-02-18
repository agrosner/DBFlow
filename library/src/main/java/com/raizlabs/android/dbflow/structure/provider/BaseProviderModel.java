package com.raizlabs.android.dbflow.structure.provider;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description: Provides a base implementation of a {@link com.raizlabs.android.dbflow.structure.Model} backed
 * by a content provider. All model operations are overridden using the {@link com.raizlabs.android.dbflow.structure.provider.ContentUtils}.
 * Consider using a {@link com.raizlabs.android.dbflow.structure.provider.BaseSyncableProviderModel} if you wish to
 * keep modifications locally from the {@link android.content.ContentProvider}
 */
public abstract class BaseProviderModel<TableClass extends BaseProviderModel> extends BaseModel implements ModelProvider<TableClass> {

    @Override
    public void delete(boolean async) {
        ContentUtils.delete(getDeleteUri(), this);
    }

    @Override
    public void save(boolean async) {
        int count = ContentUtils.update(getUpdateUri(), this);
        if (count == 0) {
            ContentUtils.insert(getInsertUri(), this);
        }
    }

    @Override
    public void update(boolean async) {
        ContentUtils.update(getUpdateUri(), this);
    }

    @Override
    public void insert(boolean async) {
        ContentUtils.insert(getInsertUri(), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(ConditionQueryBuilder<TableClass> whereConditions,
                     String orderBy, String... columns) {
        Cursor cursor = ContentUtils.query(FlowManager.getContext().getContentResolver(), getQueryUri(), whereConditions, orderBy, columns);
        if (cursor != null && cursor.moveToFirst()) {
            getModelAdapter().loadFromCursor(cursor, this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load() {
        load(getModelAdapter().getPrimaryModelWhere(this), "");
    }

}
