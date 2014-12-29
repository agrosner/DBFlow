package com.raizlabs.android.dbflow.structure;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;

/**
 * Author: andrewgrosner
 * Description: Internal adapter that gets extended when a {@link com.raizlabs.android.dbflow.annotation.Table} gets used.
 */
public abstract class ModelAdapter<ModelClass extends Model> implements InternalAdapter<ModelClass> {

    private ConditionQueryBuilder<ModelClass> mPrimaryWhere;

    private ConditionQueryBuilder<ModelClass> mFullWhere;

    private SQLiteStatement mInsertStatement;

    /**
     * @return The precompiled insert statement for this table model adapter
     */
    public SQLiteStatement getInsertStatement() {
        if (mInsertStatement == null) {
            mInsertStatement = FlowManager.getDatabaseForTable(getModelClass())
                    .getWritableDatabase().compileStatement(getInsertStatementQuery());
        }

        return mInsertStatement;
    }

    /**
     * Creates a new {@link ModelClass} and Loads the cursor into a the object.
     *
     * @param cursor The cursor to load
     * @return A new {@link ModelClass}
     */
    public abstract ModelClass loadFromCursor(Cursor cursor);

    /**
     * Saves the specified model to the DB using the specified saveMode in {@link com.raizlabs.android.dbflow.sql.SqlUtils}.
     *
     * @param async    Whether to put it on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param model    The model to save/insert/update
     * @param saveMode The {@link com.raizlabs.android.dbflow.sql.SqlUtils} save mode. Can be {@link com.raizlabs.android.dbflow.sql.SqlUtils#SAVE_MODE_DEFAULT},
     *                 {@link com.raizlabs.android.dbflow.sql.SqlUtils#SAVE_MODE_INSERT}, or {@link com.raizlabs.android.dbflow.sql.SqlUtils#SAVE_MODE_UPDATE}
     */
    public synchronized void save(boolean async, ModelClass model, int saveMode) {
        SqlUtils.sync(async, model, this, saveMode);
    }

    /**
     * Binds a {@link ModelClass} to the specified db statement
     *
     * @param sqLiteStatement The statement to fill
     */
    public abstract void bindToStatement(SQLiteStatement sqLiteStatement, ModelClass model);

    /**
     * Binds a {@link ModelClass} to the specified db statement
     *
     * @param contentValues The content values to fill.
     * @param model         The model values to put on the contentvalues
     */
    public abstract void bindToContentValues(ContentValues contentValues, ModelClass model);

    /**
     * If a {@link com.raizlabs.android.dbflow.structure.Model} has an autoincrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    public void updateAutoIncrement(ModelClass model, long id) {

    }

    /**
     * Checks to see if the model exists in the DB by running a simple query for it with its primary keys.
     *
     * @param model The model to check
     * @return true if it can be found using its primary keys.
     */
    public abstract boolean exists(ModelClass model);

    /**
     * Deletes the model from the DB
     *
     * @param async Whether to put it on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param model The model to delete
     */
    public abstract void delete(boolean async, ModelClass model);

    public abstract ConditionQueryBuilder<ModelClass> getPrimaryModelWhere(ModelClass model);

    /**
     * @return Only created once if doesn't exist, the extended class will return the builder to use.
     */
    protected abstract ConditionQueryBuilder<ModelClass> createPrimaryModelWhere();

    /**
     * Will create the where query only once that is used to check for existence in the DB.
     *
     * @return The WHERE query containing all primary key fields
     */
    public ConditionQueryBuilder<ModelClass> getPrimaryModelWhere() {
        if (mPrimaryWhere == null) {
            mPrimaryWhere = createPrimaryModelWhere();
        }
        mPrimaryWhere.setUseEmptyParams(true);
        return mPrimaryWhere;
    }

    /**
     * @return The query used to create this table.
     */
    public abstract String getCreationQuery();

    /**
     * @return The query used to insert a model using a {@link android.database.sqlite.SQLiteStatement}
     */
    protected abstract String getInsertStatementQuery();

    @Override
    public abstract Class<ModelClass> getModelClass();

    @Override
    public abstract String getTableName();

    /**
     * @return A new model using its default constructor. This is why default is required so that
     * we don't use reflection to create objects = faster.
     */
    public abstract ModelClass newInstance();
}
