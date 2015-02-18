package com.raizlabs.android.dbflow.structure.provider;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description: Provides a base implementation of a {@link com.raizlabs.android.dbflow.structure.Model} backed
 * by a content provider. All operations sync with the content provider in this app from a {@link android.content.ContentProvider}
 */
public abstract class BaseSyncableProviderModel<TableClass extends BaseSyncableProviderModel> extends BaseModel implements ModelProvider<TableClass> {

    @Override
    public void insert(boolean async) {
        super.insert(async);
        ContentUtils.insert(getInsertUri(), this);
    }

    @Override
    public void save(boolean async) {
        super.save(async);

        if(exists()) {
            update(async);
        } else {
            insert(async);
        }
    }

    @Override
    public void delete(boolean async) {
        super.delete(async);
        ContentUtils.delete(getDeleteUri(), this);
    }

    @Override
    public void update(boolean async) {
        super.update(async);
        ContentUtils.update(getUpdateUri(), this);
    }

    /**
     * Queries the {@link android.content.ContentResolver} of the app based on the passed parameters and
     * populates this object with the first row from the returned data.
     *
     * @param whereConditions The set of {@link com.raizlabs.android.dbflow.sql.builder.Condition} to filter the query by.
     * @param orderBy         The order by without the ORDER BY
     * @param columns         The list of columns to select. Leave blank for *
     */
    @SuppressWarnings("unchecked")
    public void load(ConditionQueryBuilder<TableClass> whereConditions,
                     String orderBy, String... columns) {
        Cursor cursor = ContentUtils.query(FlowManager.getContext().getContentResolver(), getQueryUri(), (Class<TableClass>) getClass(), whereConditions, orderBy, columns);
        getModelAdapter().loadFromCursor(cursor, this);
    }
}
