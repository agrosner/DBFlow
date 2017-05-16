package com.raizlabs.android.dbflow.structure;

import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.Collection;

/**
 * Description: Used for our internal Adapter classes such as generated {@link ModelAdapter}.
 */
@SuppressWarnings("NullableProblems")
public interface InternalAdapter<TModel> {

    /**
     * @return The table name of this adapter.
     */
    @NonNull
    String getTableName();

    /**
     * Saves the specified model to the DB.
     *
     * @param model The model to save/insert/update
     */
    boolean save(@NonNull TModel model);

    /**
     * Saves the specified model to the DB.
     *
     * @param model           The model to save/insert/update
     * @param databaseWrapper The manually specified wrapper.
     */
    boolean save(@NonNull TModel model, @NonNull DatabaseWrapper databaseWrapper);

    /**
     * Saves a {@link Collection} of models to the DB.
     *
     * @param models The {@link Collection} of models to save.
     */
    void saveAll(@NonNull Collection<TModel> models);

    /**
     * Saves a {@link Collection} of models to the DB.
     *
     * @param models          The {@link Collection} of models to save.
     * @param databaseWrapper The manually specified wrapper
     */
    void saveAll(@NonNull Collection<TModel> models, @NonNull DatabaseWrapper databaseWrapper);

    /**
     * Inserts the specified model into the DB.
     *
     * @param model The model to insert.
     */
    long insert(@NonNull TModel model);

    /**
     * Inserts the specified model into the DB.
     *
     * @param model           The model to insert.
     * @param databaseWrapper The manually specified wrapper.
     */
    long insert(@NonNull TModel model, @NonNull DatabaseWrapper databaseWrapper);

    /**
     * Inserts a {@link Collection} of models into the DB.
     *
     * @param models The {@link Collection} of models to save.
     */
    void insertAll(@NonNull Collection<TModel> models);

    /**
     * Inserts a {@link Collection} of models into the DB.
     *
     * @param models          The {@link Collection} of models to save.
     * @param databaseWrapper The manually specified wrapper
     */
    void insertAll(@NonNull Collection<TModel> models, @NonNull DatabaseWrapper databaseWrapper);

    /**
     * Updates the specified model into the DB.
     *
     * @param model The model to update.
     */
    boolean update(@NonNull TModel model);

    /**
     * Updates the specified model into the DB.
     *
     * @param model           The model to update.
     * @param databaseWrapper The manually specified wrapper.
     */
    boolean update(@NonNull TModel model, @NonNull DatabaseWrapper databaseWrapper);

    /**
     * Updates a {@link Collection} of models in the DB.
     *
     * @param models The {@link Collection} of models to save.
     */
    void updateAll(@NonNull Collection<TModel> models);

    /**
     * Updates a {@link Collection} of models in the DB.
     *
     * @param models          The {@link Collection} of models to save.
     * @param databaseWrapper The manually specified wrapper
     */
    void updateAll(@NonNull Collection<TModel> models, @NonNull DatabaseWrapper databaseWrapper);

    /**
     * Deletes the model from the DB
     *
     * @param model The model to delete
     */
    boolean delete(@NonNull TModel model);

    /**
     * Deletes the model from the DB
     *
     * @param model           The model to delete
     * @param databaseWrapper The manually specified wrapper.
     */
    boolean delete(@NonNull TModel model, @NonNull DatabaseWrapper databaseWrapper);

    /**
     * Updates a {@link Collection} of models in the DB.
     *
     * @param models The {@link Collection} of models to save.
     */
    void deleteAll(@NonNull Collection<TModel> models);

    /**
     * Updates a {@link Collection} of models in the DB.
     *
     * @param models          The {@link Collection} of models to save.
     * @param databaseWrapper The manually specified wrapper
     */
    void deleteAll(@NonNull Collection<TModel> models, @NonNull DatabaseWrapper databaseWrapper);

    /**
     * Binds a {@link TModel} to the specified db statement
     *
     * @param sqLiteStatement The statement to fill
     */
    void bindToStatement(@NonNull DatabaseStatement sqLiteStatement, @NonNull TModel model);

    /**
     * Provides common logic and a starting value for insert statements. So we can property compile
     * and bind statements without generating duplicate code.
     *
     * @param sqLiteStatement The statement to bind.
     * @param model           The model to retrieve data from.
     * @param start           The starting index for this bind.
     */
    void bindToInsertStatement(@NonNull DatabaseStatement sqLiteStatement, @NonNull TModel model,
                               @IntRange(from = 0, to = 1) int start);

    /**
     * Binds to a {@link SQLiteStatement}. It leaves out an autoincrementing primary key (if specified)
     * to keep the true nature of AI keys.
     *
     * @param sqLiteStatement The statement to bind to.
     * @param model           The model to read from.
     */
    void bindToInsertStatement(@NonNull DatabaseStatement sqLiteStatement, @NonNull TModel model);

    /**
     * Binds a {@link TModel} to the specified db statement
     *
     * @param contentValues The content values to fill.
     * @param model         The model values to put on the contentvalues
     */
    void bindToContentValues(@NonNull ContentValues contentValues, @NonNull TModel model);

    /**
     * Binds a {@link TModel} to the specified db statement, leaving out the {@link PrimaryKey#autoincrement()}
     * column.
     *
     * @param contentValues The content values to fill.
     * @param model         The model values to put on the content values.
     */
    void bindToInsertValues(@NonNull ContentValues contentValues, @NonNull TModel model);

    /**
     * Binds values of the model to an update {@link DatabaseStatement}. It repeats each primary
     * key binding twice to ensure proper update statements.
     */
    void bindToUpdateStatement(@NonNull DatabaseStatement updateStatement, @NonNull TModel model);

    /**
     * Binds values of the model to a delete {@link DatabaseStatement}.
     */
    void bindToDeleteStatement(@NonNull DatabaseStatement deleteStatement, @NonNull TModel model);

    /**
     * If a model has an autoincrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    void updateAutoIncrement(@NonNull TModel model, @NonNull Number id);

    /**
     * @return The value for the {@link PrimaryKey#autoincrement()}
     * if it has the field. This method is overridden when its specified for the {@link TModel}
     */
    @NonNull
    Number getAutoIncrementingId(@NonNull TModel model);

    /**
     * @return true if the {@link InternalAdapter} can be cached.
     */
    boolean cachingEnabled();
}
