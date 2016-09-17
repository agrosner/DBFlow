package com.raizlabs.android.dbflow.structure;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.property.BaseProperty;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.saveable.ListModelSaver;
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver;
import com.raizlabs.android.dbflow.structure.cache.IMultiKeyCacheConverter;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.cache.SimpleMapCache;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.Collection;

/**
 * Description: Used for generated classes from the combination of {@link Table} and {@link Model}.
 */
public abstract class ModelAdapter<TModel> extends InstanceAdapter<TModel>
        implements InternalAdapter<TModel> {

    private DatabaseStatement insertStatement;
    private DatabaseStatement compiledStatement;
    private String[] cachingColumns;
    private ModelCache<TModel, ?> modelCache;
    private ModelSaver<TModel> modelSaver;
    private ListModelSaver<TModel> listModelSaver;

    public ModelAdapter(DatabaseDefinition databaseDefinition) {
        super(databaseDefinition);
        if (getTableConfig() != null && getTableConfig().modelSaver() != null) {
            modelSaver = getTableConfig().modelSaver();
            modelSaver.setModelAdapter(this);
        }
    }

    /**
     * @return The pre-compiled insert statement for this table model adapter. This is reused and cached.
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
     * @return a new compiled {@link DatabaseStatement} representing insert. Not cached, always generated.
     * To bind values use {@link #bindToInsertStatement(DatabaseStatement, Object)}.
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
     * To bind values use {@link #bindToInsertStatement(DatabaseStatement, Object)}.
     */
    public DatabaseStatement getCompiledStatement(DatabaseWrapper databaseWrapper) {
        return databaseWrapper.compileStatement(getCompiledStatementQuery());
    }

    /**
     * Creates a new {@link TModel} and Loads the cursor into a the object.
     *
     * @param cursor The cursor to load
     * @return A new {@link TModel}
     */
    public TModel loadFromCursor(Cursor cursor) {
        TModel model = newInstance();
        loadFromCursor(cursor, model);
        return model;
    }

    @Override
    public void save(TModel model) {
        getModelSaver().save(model);
    }

    @Override
    public void save(TModel model, DatabaseWrapper databaseWrapper) {
        getModelSaver().save(model, databaseWrapper);
    }

    @Override
    public void saveAll(Collection<TModel> models) {
        getListModelSaver().saveAll(models);

        if (cachingEnabled()) {
            for (TModel model : models) {
                getModelCache().addModel(getCachingId(model), model);
            }
        }
    }

    @Override
    public void saveAll(Collection<TModel> models, DatabaseWrapper databaseWrapper) {
        getListModelSaver().saveAll(models, databaseWrapper);
    }

    @Override
    public void insert(TModel model) {
        getModelSaver().insert(model);
    }

    @Override
    public void insert(TModel model, DatabaseWrapper databaseWrapper) {
        getModelSaver().insert(model, databaseWrapper);
    }

    @Override
    public void insertAll(Collection<TModel> models) {
        getListModelSaver().insertAll(models);
    }

    @Override
    public void insertAll(Collection<TModel> models, DatabaseWrapper databaseWrapper) {
        getListModelSaver().insertAll(models, databaseWrapper);
    }

    @Override
    public void update(TModel model) {
        getModelSaver().update(model);
    }

    @Override
    public void update(TModel model, DatabaseWrapper databaseWrapper) {
        getModelSaver().update(model, databaseWrapper);
    }

    @Override
    public void updateAll(Collection<TModel> models) {
        getListModelSaver().updateAll(models);
    }

    @Override
    public void updateAll(Collection<TModel> models, DatabaseWrapper databaseWrapper) {
        getListModelSaver().updateAll(models, databaseWrapper);
    }

    @Override
    public void delete(TModel model) {
        getModelSaver().delete(model);
    }

    @Override
    public void delete(TModel model, DatabaseWrapper databaseWrapper) {
        getModelSaver().delete(model, databaseWrapper);
    }

    @Override
    public void bindToInsertStatement(DatabaseStatement sqLiteStatement, TModel model) {
        bindToInsertStatement(sqLiteStatement, model, 0);
    }

    /**
     * If a {@link Model} has an auto-incrementing primary key, then
     * this method will be overridden.
     *
     * @param model The model object to store the key
     * @param id    The key to store
     */
    @Override
    public void updateAutoIncrement(TModel model, Number id) {

    }

    /**
     * @return The value for the {@link PrimaryKey#autoincrement()}
     * if it has the field. This method is overridden when its specified for the {@link TModel}
     */
    @Override
    public Number getAutoIncrementingId(TModel model) {
        throw new InvalidDBConfiguration(
                String.format("This method may have been called in error. The model class %1s must contain" +
                                "a single primary key (if used in a ModelCache, this method may be called)",
                        getModelClass()));
    }

    /**
     * @return The autoincrement column name for the {@link PrimaryKey#autoincrement()}
     * if it has the field. This method is overridden when its specified for the {@link TModel}
     */
    public String getAutoIncrementingColumnName() {
        throw new InvalidDBConfiguration(
                String.format("This method may have been called in error. The model class %1s must contain " +
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
     * @param cursor The cursor to load caching id from.
     * @return The single cache column from cursor (if single).
     */
    public Object getCachingColumnValueFromCursor(Cursor cursor) {
        throwSingleCachingError();
        return null;
    }

    /**
     * Loads all primary keys from the {@link TModel} into the inValues. The size of the array must
     * match all primary keys. This method gets generated when caching is enabled. It converts the primary fields
     * of the {@link TModel} into the array of values the caching mechanism uses.
     *
     * @param inValues The reusable array of values to populate.
     * @param TModel   The model to load from.
     * @return The populated set of values to load from cache.
     */
    public Object[] getCachingColumnValuesFromModel(Object[] inValues, TModel TModel) {
        throwCachingError();
        return null;
    }

    /**
     * @param model The model to load cache column data from.
     * @return The single cache column from model (if single).
     */
    public Object getCachingColumnValueFromModel(TModel model) {
        throwSingleCachingError();
        return null;
    }

    public void storeModelInCache(@NonNull TModel model) {
        getModelCache().addModel(getCachingId(model), model);
    }

    public ModelCache<TModel, ?> getModelCache() {
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

    public Object getCachingId(@NonNull TModel model) {
        return getCachingId(getCachingColumnValuesFromModel(new Object[getCachingColumns().length], model));
    }

    public ModelSaver<TModel> getModelSaver() {
        if (modelSaver == null) {
            modelSaver = new ModelSaver<>();
            modelSaver.setModelAdapter(this);
        }
        return modelSaver;
    }

    public ListModelSaver<TModel> getListModelSaver() {
        if (listModelSaver == null) {
            listModelSaver = createListModelSaver();
        }
        return listModelSaver;
    }

    protected ListModelSaver<TModel> createListModelSaver() {
        return new ListModelSaver<>(getModelSaver());
    }

    /**
     * Sets how this {@link ModelAdapter} saves its objects.
     *
     * @param modelSaver The saver to use.
     */
    public void setModelSaver(ModelSaver<TModel> modelSaver) {
        this.modelSaver = modelSaver;
    }

    /**
     * Reloads relationships when loading from {@link Cursor} in a model that's cacheable. By having
     * relationships with cached models, the retrieval will be very fast.
     *
     * @param cursor The cursor to reload from.
     */
    public void reloadRelationships(@NonNull TModel model, Cursor cursor) {
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

    public ModelCache<TModel, ?> createModelCache() {
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
                                "an auto-incrementing or at least one primary key (if used in a ModelCache, this method may be called)",
                        getModelClass()));
    }

    private void throwSingleCachingError() {
        throw new InvalidDBConfiguration(
                String.format("This method may have been called in error. The model class %1s must contain" +
                                "an auto-incrementing or one primary key (if used in a ModelCache, this method may be called)",
                        getModelClass()));
    }
}
