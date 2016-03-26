package com.raizlabs.android.dbflow.structure;

import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.IntRange;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.container.ModelContainerAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Used for our internal Adapter classes such as generated {@link ModelAdapter}
 * or {@link ModelContainerAdapter}
 */
public interface InternalAdapter<TModel extends Model> {

    /**
     * @return The table name of this adapter.
     */
    String getTableName();

    /**
     * Saves the specified model to the DB.
     *
     * @param model The model to save/insert/update
     */
    void save(TModel model);

    /**
     * Saves the specified model to the DB.
     *
     * @param model           The model to save/insert/update
     * @param databaseWrapper The manually specified wrapper.
     */
    void save(TModel model, DatabaseWrapper databaseWrapper);

    /**
     * Inserts the specified model into the DB.
     *
     * @param model The model to insert.
     */
    void insert(TModel model);

    /**
     * Inserts the specified model into the DB.
     *
     * @param model           The model to insert.
     * @param databaseWrapper The manually specified wrapper.
     */
    void insert(TModel model, DatabaseWrapper databaseWrapper);

    /**
     * Updates the specified model into the DB.
     *
     * @param model The model to update.
     */
    void update(TModel model);

    /**
     * Updates the specified model into the DB.
     *
     * @param model           The model to update.
     * @param databaseWrapper The manually specified wrapper.
     */
    void update(TModel model, DatabaseWrapper databaseWrapper);

    /**
     * Deletes the model from the DB
     *
     * @param model The model to delete
     */
    void delete(TModel model);

    /**
     * Deletes the model from the DB
     *
     * @param model           The model to delete
     * @param databaseWrapper The manually specified wrapper.
     */
    void delete(TModel model, DatabaseWrapper databaseWrapper);

    /**
     * Binds a {@link TModel} to the specified db statement
     *
     * @param sqLiteStatement The statement to fill
     */
    void bindToStatement(DatabaseStatement sqLiteStatement, TModel model);

    /**
     * Provides common logic and a starting value for insert statements. So we can property compile
     * and bind statements without generating duplicate code.
     *
     * @param sqLiteStatement The statement to bind.
     * @param model           The model to retrieve data from.
     * @param start           The starting index for this bind.
     */
    void bindToInsertStatement(DatabaseStatement sqLiteStatement, TModel model,
                               @IntRange(from = 0, to = 1) int start);

    /**
     * Binds to a {@link SQLiteStatement}. It leaves out an autoincrementing primary key (if specified)
     * to keep the true nature of AI keys.
     *
     * @param sqLiteStatement The statement to bind to.
     * @param model           The model to read from.
     */
    void bindToInsertStatement(DatabaseStatement sqLiteStatement, TModel model);

    /**
     * Binds a {@link TModel} to the specified db statement
     *
     * @param contentValues The content values to fill.
     * @param model         The model values to put on the contentvalues
     */
    void bindToContentValues(ContentValues contentValues, TModel model);

    /**
     * Binds a {@link TModel} to the specified db statement, leaving out the {@link PrimaryKey#autoincrement()}
     * column.
     *
     * @param contentValues The content values to fill.
     * @param model         The model values to put on the content values.
     */
    void bindToInsertValues(ContentValues contentValues, TModel model);

    /**
     * If a {@link com.raizlabs.android.dbflow.structure.Model} has an autoincrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    void updateAutoIncrement(TModel model, Number id);

    /**
     * @return The value for the {@link PrimaryKey#autoincrement()}
     * if it has the field. This method is overridden when its specified for the {@link TModel}
     */
    Number getAutoIncrementingId(TModel model);

    /**
     * @return true if the {@link InternalAdapter} can be cached.
     */
    boolean cachingEnabled();
}
