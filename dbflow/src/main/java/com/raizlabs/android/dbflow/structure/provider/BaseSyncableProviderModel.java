package com.raizlabs.android.dbflow.structure.provider;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description: Provides a base implementation of a {@link com.raizlabs.android.dbflow.structure.Model} backed
 * by a content provider. All operations sync with the content provider in this app from a {@link android.content.ContentProvider}
 */
public abstract class BaseSyncableProviderModel
        extends BaseModel implements ModelProvider {

    @Override
    public long insert() {
        long rowId = super.insert();
        ContentUtils.insert(getInsertUri(), this);
        return rowId;
    }

    @Override
    public void save() {
        super.save();

        if (exists()) {
            ContentUtils.update(getUpdateUri(), this);
        } else {
            ContentUtils.insert(getInsertUri(), this);
        }
    }

    @Override
    public void delete() {
        super.delete();
        ContentUtils.delete(getDeleteUri(), this);
    }

    @Override
    public void update() {
        super.update();
        ContentUtils.update(getUpdateUri(), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(ConditionGroup whereConditionGroup,
                     String orderBy, String... columns) {
        Cursor cursor = ContentUtils.query(FlowManager.getContext().getContentResolver(), getQueryUri(), whereConditionGroup, orderBy, columns);
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
