package com.raizlabs.android.dbflow.structure;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;

/**
 * Author: andrewgrosner
 * Description: Internal adapter that gets extended when a {@link com.raizlabs.android.dbflow.annotation.Table} gets used.
 */
public abstract class ModelAdapter<ModelClass extends Model> implements InternalAdapter<ModelClass, ModelClass>, RetrievalAdapter<ModelClass, ModelClass> {

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
    public ModelClass loadFromCursor(Cursor cursor) {
        ModelClass model = newInstance();
        loadFromCursor(cursor, model);
        return model;
    }

    /**
     * @see #save(boolean, Model)
     */
    @Deprecated
    public synchronized void save(boolean async, ModelClass model, int saveMode) {
        SqlUtils.sync(async, model, this, saveMode);
    }

    /**
     * Saves the specified model to the DB using the specified saveMode in {@link com.raizlabs.android.dbflow.sql.SqlUtils}.
     *
     * @param async    Whether to put it on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param model    The model to save/insert/update
     */
    @Override
    public synchronized void save(boolean async, ModelClass model) {
        SqlUtils.save(async, model, this, this);
    }

    /**
     * Inserts the specified model into the DB.
     *
     * @param async Whether to put it on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param model The model to insert.
     */
    public synchronized void insert(boolean async, ModelClass model) {
        SqlUtils.insert(async, model, this, this);
    }

    /**
     * Updates the specified model into the DB.
     *
     * @param async Whether to put it on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param model The model to update.
     */
    public synchronized void update(boolean async, ModelClass model) {
        SqlUtils.update(async, model, this, this);
    }

    /**
     * Deletes the model from the DB
     *
     * @param async Whether to put it on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param model The model to delete
     */
    public void delete(boolean async, ModelClass model) {
        SqlUtils.delete(model, this, async);
    }

    /**
     * If a {@link com.raizlabs.android.dbflow.structure.Model} has an autoincrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    @Override
    public void updateAutoIncrement(ModelClass model, long id) {

    }

    /**
     * @return true if it has a {@link com.raizlabs.android.dbflow.annotation.Column#PRIMARY_KEY_AUTO_INCREMENT} field.
     * This is used to help check for existence before saving for maximum speed optimization.
     */
    public boolean hasAutoIncrementPrimaryKey() {
        return false;
    }

    /**
     * @return The value for the {@link com.raizlabs.android.dbflow.annotation.Column#PRIMARY_KEY_AUTO_INCREMENT}
     * if it has the field. This method is overridden when its specified for the {@link ModelClass}
     */
    public long getAutoIncrementingId(ModelClass model) {
        return 0;
    }

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

    /**
     * @return A new model using its default constructor. This is why default is required so that
     * we don't use reflection to create objects = faster.
     */
    public abstract ModelClass newInstance();

    /**
     * @return The conflict algorithm to use when updating a row in this table.
     */
    public ConflictAction getUpdateOnConflictAction() {
        return ConflictAction.ABORT;
    }

    /**
     * @return The conflict algorithm to use when inserting a row in this table.
     */
    public ConflictAction getInsertOnConflictAction() {
        return ConflictAction.ABORT;
    }
}
