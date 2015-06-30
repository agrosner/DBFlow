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
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.InternalAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.RetrievalAdapter;
import com.raizlabs.android.dbflow.structure.cache.BaseCacheableModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Provides some handy methods for dealing with SQL statements. It's purpose is to move the
 * methods away from the {@link com.raizlabs.android.dbflow.structure.Model} class and let any class use these.
 */
public class SqlUtils {

    /**
     * Queries the DB for a {@link android.database.Cursor} and converts it into a list.
     *
     * @param modelClass   The class to construct the data from the DB into
     * @param sql          The SQL command to perform, must not be ; terminated.
     * @param args         You may include ?s in where clause in the query,
     *                     which will be replaced by the values from selectionArgs. The
     *                     values will be bound as Strings.
     * @param <ModelClass> The class implements {@link com.raizlabs.android.dbflow.structure.Model}
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

    private static <CacheableClass extends BaseCacheableModel> List<CacheableClass> convertToCacheableList(
            Class<CacheableClass> modelClass, Cursor cursor) {
        final List<CacheableClass> entities = new ArrayList<>();
        ModelAdapter<CacheableClass> instanceAdapter = FlowManager.getModelAdapter(modelClass);
        if (instanceAdapter != null) {
            synchronized (cursor) {
                // Ensure that we aren't iterating over this cursor concurrently from different threads
                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndex(instanceAdapter.getCachingColumnName()));

                        // if it exists in cache no matter the query we will use that one
                        CacheableClass cacheable = BaseCacheableModel.getCache(modelClass).get(id);
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
     * @param <ModelClass>    The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return A model transformed from the {@link android.database.Cursor}
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
     * @param <CacheableClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return A model transformed from the {@link android.database.Cursor}
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
     * Queries the DB and returns the first {@link com.raizlabs.android.dbflow.structure.Model} it finds. Note:
     * this may return more than one object, but only will return the first item in the list.
     *
     * @param modelClass   The class to construct the data from the DB into
     * @param sql          The SQL command to perform, must not be ; terminated.
     * @param args         You may include ?s in where clause in the query,
     *                     which will be replaced by the values from selectionArgs. The
     *                     values will be bound as Strings.
     * @param <ModelClass> The class implements {@link com.raizlabs.android.dbflow.structure.Model}
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
     * Checks whether the SQL query returns a {@link android.database.Cursor} with a count of at least 1. This
     * means that the query was successful. It is commonly used when checking if a {@link com.raizlabs.android.dbflow.structure.Model} exists.
     *
     * @param table        The table to check
     * @param sql          The SQL command to perform, must not be ; terminated.
     * @param args         The optional string arguments when we use "?" in the sql
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
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

        if (FlowContentObserver.shouldNotify()) {
            notifyModelChanged(modelAdapter.getModelClass(), BaseModel.Action.SAVE, modelAdapter.getCachingColumnName(),
                    adapter.getCachingId(model));
        }
    }

    /**
     * Updates the model if it exists. If the model does not exist and no rows are changed, we will attempt an insert into the DB.
     *
     * @param model        The model to update
     * @param modelAdapter The adapter to use
     * @return true if model was inserted, false if not. Also false could mean that it is placed on the
     * {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} using async to true.
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
        } else if (FlowContentObserver.shouldNotify()) {
            notifyModelChanged(modelAdapter.getModelClass(), BaseModel.Action.UPDATE,
                    modelAdapter.getCachingColumnName(), adapter.getCachingId(model));
        }
        return exists;
    }

    /**
     * Will attempt to insert the {@link com.raizlabs.android.dbflow.structure.container.ModelContainer} into the DB.
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
        if (FlowContentObserver.shouldNotify()) {
            notifyModelChanged(modelAdapter.getModelClass(), BaseModel.Action.INSERT,
                    modelAdapter.getCachingColumnName(), adapter.getCachingId(model));
        }
    }


    /**
     * Deletes {@link com.raizlabs.android.dbflow.structure.Model} from the database using the specfied {@link com.raizlabs.android.dbflow.config.FlowManager}
     *
     * @param model The model to delete
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model, TableClass extends Model, AdapterClass extends RetrievalAdapter & InternalAdapter>
    void delete(final TableClass model, AdapterClass adapter, ModelAdapter<ModelClass> modelAdapter) {
        new Delete().from((Class<TableClass>) adapter.getModelClass()).where(
                adapter.getPrimaryModelWhere(model)).query();
        adapter.updateAutoIncrement(model, 0);
        if (FlowContentObserver.shouldNotify()) {
            notifyModelChanged(modelAdapter.getModelClass(), BaseModel.Action.DELETE,
                    modelAdapter.getCachingColumnName(), adapter.getCachingId(model));
        }
    }

    /**
     * Notifies the {@link android.database.ContentObserver} that the model has changed.
     *
     * @param action The {@link com.raizlabs.android.dbflow.structure.BaseModel.Action} enum
     * @param table  The table of the model
     */
    public static void notifyModelChanged(Class<? extends Model> table, BaseModel.Action action, String notifyKey, Object notifyValue) {
        FlowManager.getContext().getContentResolver().notifyChange(getNotificationUri(table, action, notifyKey, notifyValue), null, true);
    }

    /**
     * Returns the uri for notifications  from model changes
     *
     * @param modelClass
     * @return
     */
    public static Uri getNotificationUri(Class<? extends Model> modelClass, BaseModel.Action action, String notifyKey, Object notifyValue) {
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
     * @param action     The {@link BaseModel.Action} to use.
     * @return The uri for updates to {@link Model}, meant for general changes.
     */
    public static Uri getNotificationUri(Class<? extends Model> modelClass, BaseModel.Action action) {
        return getNotificationUri(modelClass, action, null, null);
    }


    /**
     * Drops an active TRIGGER by specifying the onTable and triggerName
     *
     * @param mOnTable     The table that this trigger runs on
     * @param triggerName  The name of the trigger
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
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
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     */
    public static <ModelClass extends Model> void dropIndex(Class<ModelClass> mOnTable, String indexName) {
        QueryBuilder queryBuilder = new QueryBuilder("DROP INDEX IF EXISTS ")
                .appendQuoted(indexName);
        FlowManager.getDatabaseForTable(mOnTable).getWritableDatabase().execSQL(queryBuilder.getQuery());
    }
}
