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

        if(exists()) {
            update(async);
        } else {
            insert(async);
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
