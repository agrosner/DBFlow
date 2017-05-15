package com.raizlabs.android.dbflow.structure;

import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.raizlabs.android.dbflow.sql.saveable.ListModelSaver;
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver;
import com.raizlabs.android.dbflow.structure.cache.IMultiKeyCacheConverter;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.cache.SimpleMapCache;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.FlowCursor;

import java.util.Collection;

import static com.raizlabs.android.dbflow.config.FlowManager.getWritableDatabaseForTable;

/**
 * Description: Used for generated classes from the combination of {@link Table} and {@link Model}.
 */
public abstract class ModelAdapter<TModel> extends InstanceAdapter<TModel>
    implements InternalAdapter<TModel> {

    private DatabaseStatement insertStatement;
    private DatabaseStatement compiledStatement;
    private DatabaseStatement updateStatement;

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
            insertStatement = getInsertStatement(getWritableDatabaseForTable(getModelClass()));
        }

        return insertStatement;
    }

    /**
     * @return The pre-compiled update statement for this table model adapter. This is reused and cached.
     */
    public DatabaseStatement getUpdateStatement() {
        if (insertStatement == null) {
            insertStatement = getUpdateStatement(getWritableDatabaseForTable(getModelClass()));
        }

        return insertStatement;
    }

    public void closeInsertStatement() {
        if (insertStatement == null) {
            return;
        }
        insertStatement.close();
        insertStatement = null;
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
     * @param databaseWrapper The database used to do an update statement.
     * @return a new compiled {@link DatabaseStatement} representing update. Not cached, always generated.
     * To bind values use {@link #bindToInsertStatement(DatabaseStatement, Object)}.
     */
    public DatabaseStatement getUpdateStatement(DatabaseWrapper databaseWrapper) {
        return databaseWrapper.compileStatement(getUpdateStatementQuery());
    }

    public DatabaseStatement getDeleteStatement(TModel model, DatabaseWrapper databaseWrapper) {
        return databaseWrapper.compileStatement(SQLite.delete().from(getModelClass())
            .where(getPrimaryConditionClause(model)).getQuery());
    }


    /**
     * @return The precompiled full statement for this table model adapter
     */
    public DatabaseStatement getCompiledStatement() {
        if (compiledStatement == null) {
            compiledStatement = getCompiledStatement(getWritableDatabaseForTable(getModelClass()));
        }

        return compiledStatement;
    }

    public void closeCompiledStatement() {
        if (compiledStatement == null) {
            return;
        }
        compiledStatement.close();
        compiledStatement = null;
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
    public TModel loadFromCursor(FlowCursor cursor) {
        TModel model = newInstance();
        loadFromCursor(cursor, model);
        return model;
    }

    @Override
    public boolean save(TModel model) {
        return getModelSaver().save(model);
    }

    @Override
    public boolean save(TModel model, DatabaseWrapper databaseWrapper) {
        return getModelSaver().save(model, databaseWrapper);
    }

    @Override
    public void saveAll(Collection<TModel> models) {
        getListModelSaver().saveAll(models);
    }

    @Override
    public void saveAll(Collection<TModel> models, DatabaseWrapper databaseWrapper) {
        getListModelSaver().saveAll(models, databaseWrapper);
    }

    @Override
    public long insert(TModel model) {
        return getModelSaver().insert(model);
    }

    @Override
    public long insert(TModel model, DatabaseWrapper databaseWrapper) {
        return getModelSaver().insert(model, databaseWrapper);
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
    public boolean update(TModel model) {
        return getModelSaver().update(model);
    }

    @Override
    public boolean update(TModel model, DatabaseWrapper databaseWrapper) {
        return getModelSaver().update(model, databaseWrapper);
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
    public boolean delete(TModel model) {
        return getModelSaver().delete(model);
    }

    @Override
    public boolean delete(TModel model, DatabaseWrapper databaseWrapper) {
        return getModelSaver().delete(model, databaseWrapper);
    }

    @Override
    public void deleteAll(Collection<TModel> tModels, DatabaseWrapper databaseWrapper) {
        getListModelSaver().deleteAll(tModels, databaseWrapper);
    }

    @Override
    public void deleteAll(Collection<TModel> tModels) {
        getListModelSaver().deleteAll(tModels);
    }

    @Override
    public void bindToInsertStatement(DatabaseStatement sqLiteStatement, TModel model) {
        bindToInsertStatement(sqLiteStatement, model, 0);
    }

    @Override
    public void bindToContentValues(ContentValues contentValues, TModel tModel) {
        bindToInsertValues(contentValues, tModel);
    }

    @Override
    public void bindToStatement(DatabaseStatement sqLiteStatement, TModel tModel) {
        bindToInsertStatement(sqLiteStatement, tModel, 0);
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
     * Called when we want to save our {@link ForeignKey} objects. usually during insert + update.
     * This method is overridden when {@link ForeignKey} specified
     */
    public void saveForeignKeys(TModel model, DatabaseWrapper wrapper) {

    }

    /**
     * Called when we want to delete our {@link ForeignKey} objects. During deletion {@link #delete(Object, DatabaseWrapper)}
     * This method is overridden when {@link ForeignKey} specified
     */
    public void deleteForeignKeys(TModel model, DatabaseWrapper wrapper) {

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
     * Loads all primary keys from the {@link FlowCursor} into the inValues. The size of the array must
     * match all primary keys. This method gets generated when caching is enabled.
     *
     * @param inValues The reusable array of values to populate.
     * @param cursor   The cursor to load from.
     * @return The populated set of values to load from cache.
     */
    public Object[] getCachingColumnValuesFromCursor(Object[] inValues, FlowCursor cursor) {
        throwCachingError();
        return null;
    }

    /**
     * @param cursor The cursor to load caching id from.
     * @return The single cache column from cursor (if single).
     */
    public Object getCachingColumnValueFromCursor(FlowCursor cursor) {
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
        this.modelSaver.setModelAdapter(this);
    }

    /**
     * Reloads relationships when loading from {@link FlowCursor} in a model that's cacheable. By having
     * relationships with cached models, the retrieval will be very fast.
     *
     * @param cursor The cursor to reload from.
     */
    public void reloadRelationships(@NonNull TModel model, FlowCursor cursor) {
        if (!cachingEnabled()) {
            throwCachingError();
        }
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
    public abstract Property getProperty(String columnName);

    /**
     * @return An array of column properties, in order of declaration.
     */
    public abstract IProperty[] getAllColumnProperties();

    /**
     * @return The query used to insert a model using a {@link SQLiteStatement}
     */
    protected String getInsertStatementQuery() {
        return getCompiledStatementQuery();
    }

    /**
     * @return The normal query used in saving a model if we use a {@link SQLiteStatement}.
     */
    protected abstract String getCompiledStatementQuery();

    protected abstract String getUpdateStatementQuery();

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
