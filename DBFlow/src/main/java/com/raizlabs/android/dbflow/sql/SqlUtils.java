package com.raizlabs.android.dbflow.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;

import com.raizlabs.android.dbflow.SQLiteCompatibilityUtils;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.DBTransactionQueue;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.BaseModel.Action;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.InternalAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.RetrievalAdapter;
import com.raizlabs.android.dbflow.structure.cache.BaseCacheableModel;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.container.ModelContainer;
import com.raizlabs.android.dbflow.structure.container.ModelContainerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Provides some handy methods for dealing with SQL statements. It's purpose is to move the
 * methods away from the {@link Model} class and let any class use these.
 */
public class SqlUtils {

    /**
     * Queries the DB for a {@link Cursor} and converts it into a list.
     *
     * @param modelClass   The class to construct the data from the DB into
     * @param sql          The SQL command to perform, must not be ; terminated.
     * @param args         You may include ?s in where clause in the query,
     *                     which will be replaced by the values from selectionArgs. The
     *                     values will be bound as Strings.
     * @param <ModelClass> The class implements {@link Model}
     * @return a list of {@link ModelClass}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> List<ModelClass> queryList(Class<ModelClass> modelClass, String sql,
                                                                        String... args) {
        BaseDatabaseDefinition flowManager = FlowManager.getDatabaseForTable(modelClass);
        Cursor cursor = flowManager.getWritableDatabase().rawQuery(sql, args);
        List<ModelClass> list = null;
        if (BaseCacheableModel.class.isAssignableFrom(modelClass)) {
            list = (List<ModelClass>) convertToCacheableList((Class<? extends BaseCacheableModel>) modelClass, cursor);
        } else {
            list = convertToList(modelClass, cursor);
        }
        cursor.close();
        return list;
    }

    /**
     * Loops through a {@link Cursor} and builds a list of {@link CacheableClass} objects. If an item
     * with the same id exists within the cache for that model, the cached object for that class is used.
     *
     * @param modelClass       The class to convert the cursor into {@link CacheableClass}
     * @param cursor           The cursor from a query.
     * @param modelCache       The model cache to use when retrieving {@link CacheableClass}.
     * @param <CacheableClass> The class that extends {@link BaseCacheableModel}
     * @return A {@link List} of {@link CacheableClass}.
     */
    public static <CacheableClass extends BaseCacheableModel> List<CacheableClass> convertToCacheableList(
            Class<CacheableClass> modelClass, Cursor cursor, ModelCache<CacheableClass, ?> modelCache) {
        final List<CacheableClass> entities = new ArrayList<>();
        ModelAdapter<CacheableClass> instanceAdapter = FlowManager.getModelAdapter(modelClass);
        if (instanceAdapter != null) {
            if (!instanceAdapter.hasCachingId()) {
                throw new IllegalArgumentException("You cannot call this method for a table that has no caching id. Either" +
                        "use one Primary Key or call convertToList()");
            } else if (modelCache == null) {
                throw new IllegalArgumentException("ModelCache specified in convertToCacheableList() must not be null.");
            }
            synchronized (cursor) {
                // Ensure that we aren't iterating over this cursor concurrently from different threads
                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndex(instanceAdapter.getCachingColumnName()));

                        // if it exists in cache no matter the query we will use that one
                        CacheableClass cacheable = modelCache.get(id);
                        if (cacheable != null) {
                            entities.add(cacheable);
                        } else {
                            cacheable = instanceAdapter.newInstance();
                            instanceAdapter.loadFromCursor(cursor, cacheable);
                            entities.add(cacheable);
                        }
                    } while (cursor.moveToNext());
                }
            }
        }
        return entities;
    }

    /**
     * Loops through a {@link Cursor} and builds a list of {@link CacheableClass} objects. If an item
     * with the same id exists within the cache for that model, the cached object for that class is used.
     *
     * @param modelClass       The class to convert the cursor into {@link CacheableClass}
     * @param cursor           The cursor from a query.
     * @param <CacheableClass> The class that extends {@link BaseCacheableModel}
     * @return A {@link List} of {@link CacheableClass}.
     */
    public static <CacheableClass extends BaseCacheableModel> List<CacheableClass> convertToCacheableList(
            Class<CacheableClass> modelClass, Cursor cursor) {
        return convertToCacheableList(modelClass, cursor, BaseCacheableModel.getCache(modelClass));
    }

    /**
     * Loops through a cursor and builds a list of {@link ModelClass} objects.
     *
     * @param table        The model class that we convert the cursor data into.
     * @param cursor       The cursor from the DB
     * @param <ModelClass> The class that implements {@link Model}
     * @return An non-null {@link List}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> List<ModelClass> convertToList(Class<ModelClass> table, Cursor cursor) {
        final List<ModelClass> entities = new ArrayList<>();
        InstanceAdapter modelAdapter = FlowManager.getInstanceAdapter(table);
        if (modelAdapter != null) {
            // Ensure that we aren't iterating over this cursor concurrently from different threads
            synchronized (cursor) {
                if (cursor.moveToFirst()) {
                    do {
                        Model model = modelAdapter.newInstance();
                        modelAdapter.loadFromCursor(cursor, model);
                        entities.add((ModelClass) model);
                    }
                    while (cursor.moveToNext());
                }
            }
        }

        return entities;
    }

    /**
     * Takes first {@link ModelClass} from the cursor
     *
     * @param dontMoveToFirst If it's a list or at a specific position, do not reset the cursor
     * @param table           The model class that we convert the cursor data into.
     * @param cursor          The cursor from the DB
     * @param <ModelClass>    The class that implements {@link Model}
     * @return A model transformed from the {@link Cursor}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ModelClass convertToModel(boolean dontMoveToFirst, Class<ModelClass> table,
                                                                       Cursor cursor) {
        ModelClass model = null;
        if (dontMoveToFirst || cursor.moveToFirst()) {
            InstanceAdapter modelAdapter = FlowManager.getInstanceAdapter(table);

            if (modelAdapter != null) {
                model = (ModelClass) modelAdapter.newInstance();
                modelAdapter.loadFromCursor(cursor, model);
            }
        }

        return model;
    }

    /**
     * Takes a {@link CacheableClass} from either cache (if exists) else it reads from the cursor
     *
     * @param dontMoveToFirst  If it's a list or at a specific position, do not reset the cursor
     * @param table            The model class that we convert the cursor data into.
     * @param cursor           The cursor from the DB
     * @param <CacheableClass> The class that implements {@link Model}
     * @return A model transformed from the {@link Cursor}
     */
    @SuppressWarnings("unchecked")
    public static <CacheableClass extends BaseCacheableModel> CacheableClass convertToCacheableModel(
            boolean dontMoveToFirst, Class<CacheableClass> table, Cursor cursor) {
        CacheableClass model = null;
        if (dontMoveToFirst || cursor.moveToFirst()) {
            ModelAdapter<CacheableClass> modelAdapter = FlowManager.getModelAdapter(table);

            if (modelAdapter != null) {
                Object id = modelAdapter.getCachingIdFromCursorIndex(cursor,
                        cursor.getColumnIndex(modelAdapter.getCachingColumnName()));
                model = BaseCacheableModel.getCache(table).get(id);
                if (model == null) {
                    model = modelAdapter.newInstance();
                    modelAdapter.loadFromCursor(cursor, model);
                }
            }
        }

        return model;
    }

    /**
     * Queries the DB and returns the first {@link Model} it finds. Note:
     * this may return more than one object, but only will return the first item in the list.
     *
     * @param modelClass   The class to construct the data from the DB into
     * @param sql          The SQL command to perform, must not be ; terminated.
     * @param args         You may include ?s in where clause in the query,
     *                     which will be replaced by the values from selectionArgs. The
     *                     values will be bound as Strings.
     * @param <ModelClass> The class implements {@link Model}
     * @return a single {@link ModelClass}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ModelClass querySingle(Class<ModelClass> modelClass, String sql,
                                                                    String... args) {
        Cursor cursor = FlowManager.getDatabaseForTable(modelClass).getWritableDatabase().rawQuery(sql, args);
        ModelClass retModel = null;
        if (BaseCacheableModel.class.isAssignableFrom(modelClass)) {
            retModel = (ModelClass) convertToCacheableModel(false, (Class<? extends BaseCacheableModel>) modelClass,
                    cursor);
        } else {
            retModel = convertToModel(false, modelClass, cursor);
        }
        cursor.close();
        return retModel;
    }

    /**
     * Checks whether the SQL query returns a {@link Cursor} with a count of at least 1. This
     * means that the query was successful. It is commonly used when checking if a {@link Model} exists.
     *
     * @param table        The table to check
     * @param sql          The SQL command to perform, must not be ; terminated.
     * @param args         The optional string arguments when we use "?" in the sql
     * @param <ModelClass> The class that implements {@link Model}
     * @return
     */
    public static <ModelClass extends Model> boolean hasData(Class<ModelClass> table, String sql, String... args) {
        BaseDatabaseDefinition flowManager = FlowManager.getDatabaseForTable(table);
        Cursor cursor = flowManager.getWritableDatabase().rawQuery(sql, args);
        boolean hasData = (cursor.getCount() > 0);
        cursor.close();
        return hasData;
    }

    /**
     * Saves the model into the DB based on whether it exists or not.
     *
     * @param model        The model to save
     * @param modelAdapter The {@link ModelAdapter} to use
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model, TableClass extends Model, AdapterClass extends RetrievalAdapter & InternalAdapter>
    void save(TableClass model, AdapterClass adapter, ModelAdapter<ModelClass> modelAdapter) {
        if (model == null) {
            throw new IllegalArgumentException("Model from " + modelAdapter.getModelClass() + " was null");
        }

        boolean exists = adapter.exists(model);

        if (exists) {
            exists = update(model, adapter, modelAdapter);
        }

        if (!exists) {
            insert(model, adapter, modelAdapter);
        }

        notifyModelChanged(model, adapter, modelAdapter, Action.SAVE);
    }

    /**
     * Updates the model if it exists. If the model does not exist and no rows are changed, we will attempt an insert into the DB.
     *
     * @param model        The model to update
     * @param modelAdapter The adapter to use
     * @return true if model was inserted, false if not. Also false could mean that it is placed on the
     * {@link DBTransactionQueue} using async to true.
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model, TableClass extends Model, AdapterClass extends RetrievalAdapter & InternalAdapter>
    boolean update(TableClass model, AdapterClass adapter, ModelAdapter<ModelClass> modelAdapter) {
        boolean exists;
        SQLiteDatabase db = FlowManager.getDatabaseForTable(modelAdapter.getModelClass()).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        adapter.bindToContentValues(contentValues, model);
        exists = (SQLiteCompatibilityUtils.updateWithOnConflict(db, modelAdapter.getTableName(), contentValues,
                adapter.getPrimaryModelWhere(model).getQuery(), null,
                ConflictAction.getSQLiteDatabaseAlgorithmInt(
                        modelAdapter.getUpdateOnConflictAction())) !=
                0);
        if (!exists) {
            // insert
            insert(model, adapter, modelAdapter);
        } else {
            notifyModelChanged(model, adapter, modelAdapter, Action.UPDATE);
        }
        return exists;
    }

    /**
     * Will attempt to insert the {@link ModelContainer} into the DB.
     *
     * @param model        The model to insert.
     * @param modelAdapter The adapter to use.
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model, TableClass extends Model, AdapterClass extends RetrievalAdapter & InternalAdapter>
    void insert(TableClass model, AdapterClass adapter, ModelAdapter<ModelClass> modelAdapter) {
        SQLiteStatement insertStatement = modelAdapter.getInsertStatement();
        adapter.bindToStatement(insertStatement, model);
        long id = insertStatement.executeInsert();
        adapter.updateAutoIncrement(model, id);
        notifyModelChanged(model, adapter, modelAdapter, Action.INSERT);
    }


    /**
     * Deletes {@link Model} from the database using the specfied {@link FlowManager}
     *
     * @param model The model to delete
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model, TableClass extends Model, AdapterClass extends RetrievalAdapter & InternalAdapter>
    void delete(final TableClass model, AdapterClass adapter, ModelAdapter<ModelClass> modelAdapter) {
        new Delete().from((Class<TableClass>) adapter.getModelClass()).where(
                adapter.getPrimaryModelWhere(model)).query();
        adapter.updateAutoIncrement(model, 0);
        notifyModelChanged(model, adapter, modelAdapter, Action.DELETE);
    }

    /**
     * Notifies the {@link android.database.ContentObserver} that the model has changed.
     *
     * @param action The {@link Action} enum
     * @param table  The table of the model
     */
    public static void notifyModelChanged(Class<? extends Model> table, Action action, String notifyKey, Object notifyValue) {
        FlowManager.getContext().getContentResolver().notifyChange(getNotificationUri(table, action, notifyKey, notifyValue), null, true);
    }

    /**
     * Performs necessary logic to notify of {@link Model} changes.
     *
     * @param model          The model to use to notify (if a caching id exists for that model).
     * @param adapter        The adapter to use thats either a {@link ModelAdapter} or {@link ModelContainerAdapter}
     *                       to handle interactions.
     * @param modelAdapter   The actual {@link ModelAdapter} associated with the {@link ModelClass}/
     * @param action         The {@link Action} that occured.
     * @param <ModelClass>   The original model class.
     * @param <TableClass>   The class of the adapter that we use the model from.
     * @param <AdapterClass> The class of the adapter, which is either a {@link ModelAdapter} or {@link ModelContainerAdapter}
     */
    @SuppressWarnings("unchecked")
    private static <ModelClass extends Model, TableClass extends Model, AdapterClass extends RetrievalAdapter & InternalAdapter>
    void notifyModelChanged(TableClass model, AdapterClass adapter, ModelAdapter<ModelClass> modelAdapter, Action action) {
        if (FlowContentObserver.shouldNotify()) {
            if (modelAdapter.hasCachingId()) {
                notifyModelChanged(modelAdapter.getModelClass(), action,
                        modelAdapter.getCachingColumnName(), adapter.getCachingId(model));
            } else {
                notifyModelChanged(modelAdapter.getModelClass(), action, null, null);
            }
        }
    }

    /**
     * Returns the uri for notifications  from model changes
     *
     * @param modelClass
     * @return
     */
    public static Uri getNotificationUri(Class<? extends Model> modelClass, Action action, String notifyKey, Object notifyValue) {
        Uri.Builder uriBuilder = new Uri.Builder().scheme("dbflow")
                .authority(FlowManager.getTableName(modelClass));
        if (action != null) {
            uriBuilder.fragment(action.name());
        }
        if (notifyKey != null) {
            uriBuilder.appendQueryParameter(Uri.encode(notifyKey), Uri.encode(String.valueOf(notifyValue)));
        }
        return uriBuilder.build();
    }

    /**
     * @param modelClass The model class to use.
     * @param action     The {@link Action} to use.
     * @return The uri for updates to {@link Model}, meant for general changes.
     */
    public static Uri getNotificationUri(Class<? extends Model> modelClass, Action action) {
        return getNotificationUri(modelClass, action, null, null);
    }


    /**
     * Drops an active TRIGGER by specifying the onTable and triggerName
     *
     * @param mOnTable     The table that this trigger runs on
     * @param triggerName  The name of the trigger
     * @param <ModelClass> The class that implements {@link Model}
     */
    public static <ModelClass extends Model> void dropTrigger(Class<ModelClass> mOnTable, String triggerName) {
        QueryBuilder queryBuilder = new QueryBuilder("DROP TRIGGER IF EXISTS ")
                .append(triggerName);
        FlowManager.getDatabaseForTable(mOnTable).getWritableDatabase().execSQL(queryBuilder.getQuery());
    }

    /**
     * Drops an active INDEX by specifying the onTable and indexName
     *
     * @param mOnTable     The table that this index runs on
     * @param indexName    The name of the index.
     * @param <ModelClass> The class that implements {@link Model}
     */
    public static <ModelClass extends Model> void dropIndex(Class<ModelClass> mOnTable, String indexName) {
        QueryBuilder queryBuilder = new QueryBuilder("DROP INDEX IF EXISTS ")
                .appendQuoted(indexName);
        FlowManager.getDatabaseForTable(mOnTable).getWritableDatabase().execSQL(queryBuilder.getQuery());
    }
}
