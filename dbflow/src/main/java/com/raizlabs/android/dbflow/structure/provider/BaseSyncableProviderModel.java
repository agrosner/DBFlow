package com.raizlabs.android.dbflow.structure.provider;

import android.content.ContentProvider;
import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Provides a base implementation of a {@link Model} backed
 * by a content provider. All operations sync with the content provider in this app from a {@link ContentProvider}
 */
public abstract class BaseSyncableProviderModel extends BaseModel implements ModelProvider {

    @Override
    public long insert() {
        long rowId = super.insert();
        ContentUtils.insert(getInsertUri(), this);
        return rowId;
    }

    @Override
    public boolean save() {
        if (exists()) {
            return super.save() && ContentUtils.update(getUpdateUri(), this) > 0;
        } else {
            return super.save() && ContentUtils.insert(getInsertUri(), this) != null;
        }
    }

    @Override
    public boolean delete() {
        return super.delete() && ContentUtils.delete(getDeleteUri(), this) > 0;
    }

    @Override
    public boolean update() {
        return super.update() && ContentUtils.update(getUpdateUri(), this) > 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(OperatorGroup whereOperatorGroup,
                     String orderBy, String... columns) {
        Cursor cursor = ContentUtils.query(FlowManager.getContext().getContentResolver(), getQueryUri(), whereOperatorGroup, orderBy, columns);
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
