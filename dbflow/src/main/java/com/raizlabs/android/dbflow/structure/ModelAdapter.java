package com.raizlabs.android.dbflow.structure;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.property.BaseProperty;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver;
import com.raizlabs.android.dbflow.structure.cache.IMultiKeyCacheConverter;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.cache.SimpleMapCache;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Author: andrewgrosner
 * Description: Internal adapter that gets extended when a {@link Table} gets used.
 */
public abstract class ModelAdapter<ModelClass extends Model> extends InstanceAdapter<ModelClass, ModelClass>
    implements InternalAdapter<ModelClass, ModelClass> {

    private DatabaseStatement insertStatement;
    private DatabaseStatement compiledStatement;
    private String[] cachingColumns;
    private ModelCache<ModelClass, ?> modelCache;
    private ModelSaver<ModelClass, ModelClass, ModelAdapter<ModelClass>> modelSaver;

    /**
     * @return The precompiled insert statement for this table model adapter
     */
    public DatabaseStatement getInsertStatement() {
        if (insertStatement == null) {
            insertStatement = getInsertStatement(
                FlowManager.getDatabaseForTable(getModelClass()).getWritableDatabase());
        }

        return insertStatement;
    }

    /**
     * @param databaseWrapper The database used to do an insert statement.
     * @return a new compiled {@link DatabaseStatement} representing insert.
     * To bind values use {@link #bindToInsertStatement(DatabaseStatement, Model)}.
     */
    public DatabaseStatement getInsertStatement(DatabaseWrapper databaseWrapper) {
        return databaseWrapper.compileStatement(getInsertStatementQuery());
    }

    /**
     * @return The precompiled full statement for this table model adapter
     */
    public DatabaseStatement getCompiledStatement() {
        if (compiledStatement == null) {
            compiledStatement = getCompiledStatement(
                FlowManager.getDatabaseForTable(getModelClass()).getWritableDatabase());
        }

        return compiledStatement;
    }

    /**
     * @param databaseWrapper The database used to do an insert statement.
     * @return a new compiled {@link DatabaseStatement} representing insert.
     * To bind values use {@link #bindToInsertStatement(DatabaseStatement, Model)}.
     */
    public DatabaseStatement getCompiledStatement(DatabaseWrapper databaseWrapper) {
        return databaseWrapper.compileStatement(getCompiledStatementQuery());
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
     * Saves the specified model to the DB using the specified saveMode in {@link SqlUtils}.
     *
     * @param model The model to save/insert/update
     */
    @Override
    public void save(ModelClass model) {
        getModelSaver().save(model);
    }

    /**
     * Inserts the specified model into the DB.
     *
     * @param model The model to insert.
     */
    @Override
    public void insert(ModelClass model) {
        getModelSaver().insert(model);
    }

    /**
     * Updates the specified model into the DB.
     *
     * @param model The model to update.
     */
    @Override
    public void update(ModelClass model) {
        getModelSaver().update(model);
    }

    /**
     * Deletes the model from the DB
     *
     * @param model The model to delete
     */
    @Override
    public void delete(ModelClass model) {
        getModelSaver().delete(model);
    }


    @Override
    public void bindToInsertStatement(DatabaseStatement sqLiteStatement, ModelClass model) {
        bindToInsertStatement(sqLiteStatement, model, 0);
    }

    /**
     * If a {@link com.raizlabs.android.dbflow.structure.Model} has an autoincrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    @Override
    public void updateAutoIncrement(ModelClass model, Number id) {

    }

    /**
     * @return The value for the {@link PrimaryKey#autoincrement()}
     * if it has the field. This method is overridden when its specified for the {@link ModelClass}
     */
    @Override
    public Number getAutoIncrementingId(ModelClass model) {
        throw new InvalidDBConfiguration(
            String.format("This method may have been called in error. The model class %1s must contain" +
                    "a single primary key (if used in a ModelCache, this method may be called)",
                getModelClass()));
    }

    /**
     * @return The autoincrement column name for the {@link PrimaryKey#autoincrement()}
     * if it has the field. This method is overridden when its specified for the {@link ModelClass}
     */
    public String getAutoIncrementingColumnName() {
        throw new InvalidDBConfiguration(
            String.format("This method may have been called in error. The model class %1s must contain" +
                    "an autoincrementing or single int/long primary key (if used in a ModelCache, this method may be called)",
                getModelClass()));
    }

    /**
     * @return A set of columns that represent the caching columns.
     */
    public String[] createCachingColumns() {
        return new String[]{getAutoIncrementingColumnName()};
    }

    /**
     * @return The array of columns (in order) that represent what are cached.
     */
    public String[] getCachingColumns() {
        if (cachingColumns == null) {
            cachingColumns = createCachingColumns();
        }
        return cachingColumns;
    }

    /**
     * Loads all primary keys from the {@link Cursor} into the inValues. The size of the array must
     * match all primary keys. This method gets generated when caching is enabled.
     *
     * @param inValues The reusable array of values to populate.
     * @param cursor   The cursor to load from.
     * @return The populated set of values to load from cache.
     */
    public Object[] getCachingColumnValuesFromCursor(Object[] inValues, Cursor cursor) {
        throwCachingError();
        return null;
    }

    /**
     * Loads all primary keys from the {@link ModelClass} into the inValues. The size of the array must
     * match all primary keys. This method gets generated when caching is enabled. It converts the primary fields
     * of the {@link ModelClass} into the array of values the caching mechanism uses.
     *
     * @param inValues   The reusable array of values to populate.
     * @param modelClass The model to load from.
     * @return The populated set of values to load from cache.
     */
    public Object[] getCachingColumnValuesFromModel(Object[] inValues, ModelClass modelClass) {
        throwCachingError();
        return null;
    }

    public ModelCache<ModelClass, ?> getModelCache() {
        if (modelCache == null) {
            modelCache = createModelCache();
        }
        return modelCache;
    }

    public Object getCachingId(@NonNull Object[] inValues) {
        if (inValues.length == 1) {
            // if it exists in cache no matter the query we will use that one
            return inValues[0];
        } else {
            return getCacheConverter().getCachingKey(inValues);
        }
    }

    public Object getCachingId(@NonNull ModelClass model) {
        return getCachingId(getCachingColumnValuesFromModel(new Object[getCachingColumns().length], model));
    }

    public ModelSaver<ModelClass, ModelClass, ModelAdapter<ModelClass>> getModelSaver() {
        if (modelSaver == null) {
            modelSaver = new ModelSaver<>(this, this);
        }
        return modelSaver;
    }

    /**
     * Sets how this {@link ModelAdapter} saves its objects.
     *
     * @param modelSaver The saver to use.
     */
    public void setModelSaver(ModelSaver<ModelClass, ModelClass, ModelAdapter<ModelClass>> modelSaver) {
        this.modelSaver = modelSaver;
    }

    /**
     * Reloads relationships when loading from {@link Cursor} in a model that's cacheable. By having
     * relationships with cached models, the retrieval will be very fast.
     *
     * @param cursor The cursor to reload from.
     */
    public void reloadRelationships(@NonNull ModelClass model, Cursor cursor) {
        throwCachingError();
    }

    @Override
    public boolean cachingEnabled() {
        return false;
    }

    public int getCacheSize() {
        return Table.DEFAULT_CACHE_SIZE;
    }

    public IMultiKeyCacheConverter<?> getCacheConverter() {
        throw new InvalidDBConfiguration("For multiple primary keys, a public static IMultiKeyCacheConverter field must" +
            "be  marked with @MultiCacheField in the corresponding model class. The resulting key" +
            "must be a unique combination of the multiple keys, otherwise inconsistencies may occur.");
    }

    public ModelCache<ModelClass, ?> createModelCache() {
        return new SimpleMapCache<>(getCacheSize());
    }


    /**
     * @return The query used to create this table.
     */
    public abstract String getCreationQuery();

    /**
     * Retrieves a property by name from the table via the corresponding generated "_Table" class. Useful
     * when you want to dynamically get a property from an {@link ModelAdapter} and do an operation on it.
     *
     * @param columnName The column name of the property.
     * @return The property from the corresponding Table class.
     */
    public abstract BaseProperty getProperty(String columnName);

    /**
     * @return An array of column properties, in order of declaration.
     */
    public abstract IProperty[] getAllColumnProperties();

    /**
     * @return The query used to insert a model using a {@link SQLiteStatement}
     */
    protected abstract String getInsertStatementQuery();

    /**
     * @return The normal query used in saving a model if we use a {@link SQLiteStatement}.
     */
    protected abstract String getCompiledStatementQuery();

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

    private void throwCachingError() {
        throw new InvalidDBConfiguration(
            String.format("This method may have been called in error. The model class %1s must contain" +
                    "an autoincrementing or at least one int/long primary key (if used in a ModelCache, this method may be called)",
                getModelClass()));
    }
}
