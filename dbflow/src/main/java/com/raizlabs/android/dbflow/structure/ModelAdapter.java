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
import com.raizlabs.android.dbflow.structure.cache.IMultiKeyCacheConverter;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.cache.SimpleMapCache;

/**
 * Author: andrewgrosner
 * Description: Internal adapter that gets extended when a {@link Table} gets used.
 */
public abstract class ModelAdapter<ModelClass extends Model>
        implements InternalAdapter<ModelClass, ModelClass>, InstanceAdapter<ModelClass, ModelClass> {

    private SQLiteStatement mInsertStatement;
    private String[] cachingColumns;
    private ModelCache<ModelClass, ?> modelCache;

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
     * Saves the specified model to the DB using the specified saveMode in {@link SqlUtils}.
     *
     * @param model The model to save/insert/update
     */
    @Override
    public void save(ModelClass model) {
        SqlUtils.save(model, this, this);
    }

    /**
     * Inserts the specified model into the DB.
     *
     * @param model The model to insert.
     */
    @Override
    public void insert(ModelClass model) {
        SqlUtils.insert(model, this, this);
    }

    /**
     * Updates the specified model into the DB.
     *
     * @param model The model to update.
     */
    @Override
    public void update(ModelClass model) {
        SqlUtils.update(model, this, this);
    }

    /**
     * Deletes the model from the DB
     *
     * @param model The model to delete
     */
    @Override
    public void delete(ModelClass model) {
        SqlUtils.delete(model, this, this);
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

    public String[] createCachingColumns() {
        return new String[]{getAutoIncrementingColumnName()};
    }

    public String[] getCachingColumns() {
        if (cachingColumns == null) {
            cachingColumns = createCachingColumns();
        }
        return cachingColumns;
    }

    public Object[] getCachingColumnValuesFromCursor(Object[] inValues, Cursor cursor) {
        throwCachingError();
        return null;
    }

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

    @Override
    public void bindToInsertStatement(SQLiteStatement sqLiteStatement, ModelClass model) {
        bindToInsertStatement(sqLiteStatement, model, 0);
    }

    public IMultiKeyCacheConverter<?, ModelClass> getCacheConverter() {
        throwCachingError();
        return null;
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
