package com.raizlabs.android.dbflow.structure.provider;

import android.content.ContentProvider;
import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Provides a base implementation of a {@link Model} backed
 * by a content provider. All model operations are overridden using the {@link ContentUtils}.
 * Consider using a {@link BaseSyncableProviderModel} if you wish to
 * keep modifications locally from the {@link ContentProvider}
 */
public abstract class BaseProviderModel<TProviderModel extends BaseProviderModel>
        extends BaseModel implements ModelProvider {

    @Override
    public void delete() {
        ContentUtils.delete(getDeleteUri(), this);
    }

    @Override
    public void save() {
        int count = ContentUtils.update(getUpdateUri(), this);
        if (count == 0) {
            ContentUtils.insert(getInsertUri(), this);
        }
    }

    @Override
    public void update() {
        ContentUtils.update(getUpdateUri(), this);
    }

    @Override
    public void insert() {
        ContentUtils.insert(getInsertUri(), this);
    }

    /**
     * Runs a query on the {@link ContentProvider} to see if it returns data.
     *
     * @return true if this model exists in the {@link ContentProvider} based on its primary keys.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean exists() {
        Cursor cursor = ContentUtils.query(FlowManager.getContext().getContentResolver(),
                getQueryUri(), getModelAdapter().getPrimaryConditionClause(this), "");
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(ConditionGroup whereConditions,
                     String orderBy, String... columns) {
        Cursor cursor = ContentUtils.query(FlowManager.getContext().getContentResolver(),
                getQueryUri(), whereConditions, orderBy, columns);
        if (cursor != null && cursor.moveToFirst()) {
            getModelAdapter().loadFromCursor(cursor, this);
            cursor.close();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load() {
        load(getModelAdapter().getPrimaryConditionClause(this), "");
    }

}
