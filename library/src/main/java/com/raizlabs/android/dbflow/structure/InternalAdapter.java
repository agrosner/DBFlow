package com.raizlabs.android.dbflow.structure;

import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;

/**
 * Description: Used for our internal Adapter classes such as generated {@link com.raizlabs.android.dbflow.structure.ModelAdapter}
 * or {@link com.raizlabs.android.dbflow.structure.container.ContainerAdapter}
 */
public interface InternalAdapter<TableClass extends Model, ModelClass extends Model> {

    /**
     * @return the model class this adapter corresponds to
     */
    public abstract Class<TableClass> getModelClass();

    /**
     * @return The table name of this adapter.
     */
    public abstract String getTableName();

    /**
     * Saves the specified model to the DB using the specified saveMode in {@link com.raizlabs.android.dbflow.sql.SqlUtils}.
     *
     * @param async    Whether to put it on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param model    The model to save/insert/update
     * @param saveMode The {@link com.raizlabs.android.dbflow.sql.SqlUtils} save mode. Can be {@link com.raizlabs.android.dbflow.sql.SqlUtils#SAVE_MODE_DEFAULT},
     *                 {@link com.raizlabs.android.dbflow.sql.SqlUtils#SAVE_MODE_INSERT}, or {@link com.raizlabs.android.dbflow.sql.SqlUtils#SAVE_MODE_UPDATE}
     */
    public void save(boolean async, ModelClass model, int saveMode);

    /**
     * Inserts the specified model into the DB.
     *
     * @param async Whether to put it on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param model The model to insert.
     */
    public void insert(boolean async, ModelClass model);

    /**
     * Updates the specified model into the DB.
     *
     * @param async Whether to put it on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param model The model to update.
     */
    public void update(boolean async, ModelClass model);

    /**
     * Deletes the model from the DB
     *
     * @param async Whether to put it on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param model The model to delete
     */
    public void delete(boolean async, ModelClass model);

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
    public void updateAutoIncrement(ModelClass model, long id);

    /**
     * @return The value for the {@link com.raizlabs.android.dbflow.annotation.Column#PRIMARY_KEY_AUTO_INCREMENT}
     * if it has the field. This method is overridden when its specified for the {@link ModelClass}
     */
    public long getAutoIncrementingId(ModelClass model);
}
