package com.raizlabs.android.dbflow.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.StringUtils;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.queriable.CacheableListModelLoader;
import com.raizlabs.android.dbflow.sql.queriable.CacheableModelLoader;
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader;
import com.raizlabs.android.dbflow.sql.queriable.ModelContainerLoader;
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader;
import com.raizlabs.android.dbflow.structure.BaseModel.Action;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.InternalAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.RetrievalAdapter;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.container.ModelContainer;
import com.raizlabs.android.dbflow.structure.container.ModelContainerAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description: Provides some handy methods for dealing with SQL statements. It's purpose is to move the
 * methods away from the {@link Model} class and let any class use these.
 */
public class SqlUtils {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();


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
     * @deprecated see {@link ListModelLoader}
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <ModelClass extends Model> List<ModelClass> queryList(Class<ModelClass> modelClass, String sql,
                                                                        String... args) {
        DatabaseDefinition flowManager = FlowManager.getDatabaseForTable(modelClass);
        Cursor cursor = flowManager.getWritableDatabase().rawQuery(sql, args);
        List<ModelClass> list = null;
        try {
            ModelAdapter modelAdapter = FlowManager.getModelAdapter(modelClass);
            if (modelAdapter != null && modelAdapter.cachingEnabled()) {
                list = convertToCacheableList(modelClass, cursor);
            } else {
                list = convertToList(modelClass, cursor);
            }
        } finally {
            cursor.close();
        }

        return list;
    }

    /**
     * Loops through a {@link Cursor} and builds a list of {@link CacheableClass} objects. If an item
     * with the same id exists within the cache for that model, the cached object for that class is used.
     *
     * @param modelClass       The class to convert the cursor into {@link CacheableClass}
     * @param cursor           The cursor from a query.
     * @param modelCache       The model cache to use when retrieving {@link CacheableClass}.
     * @param <CacheableClass> The class that extends {@link Model} with {@link Table#cachingEnabled()}.
     * @return A {@link List} of {@link CacheableClass}.
     * @deprecated see {@link CacheableListModelLoader}
     */
    @Deprecated
    public static <CacheableClass extends Model> List<CacheableClass> convertToCacheableList(
        Class<CacheableClass> modelClass, Cursor cursor, ModelCache<CacheableClass, ?> modelCache) {
        final List<CacheableClass> entities = new ArrayList<>();
        ModelAdapter<CacheableClass> instanceAdapter = FlowManager.getModelAdapter(modelClass);
        if (instanceAdapter != null) {
            if (!instanceAdapter.cachingEnabled()) {
                throw new IllegalArgumentException("You cannot call this method for a table that has no caching id. Either" +
                    "use one Primary Key or call convertToList()");
            } else if (modelCache == null) {
                throw new IllegalArgumentException("ModelCache specified in convertToCacheableList() must not be null.");
            }
            Object[] cacheValues = new Object[instanceAdapter.getCachingColumns().length];
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (cursor) {
                // Ensure that we aren't iterating over this cursor concurrently from different threads
                if (cursor.moveToFirst()) {
                    do {
                        Object[] values = instanceAdapter.getCachingColumnValuesFromCursor(cacheValues, cursor);
                        CacheableClass cacheable = modelCache.get(instanceAdapter.getCachingId(values));
                        if (cacheable != null) {
                            instanceAdapter.reloadRelationships(cacheable, cursor);
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
     * @param <CacheableClass> The class that extends {@link Model} with {@link Table#cachingEnabled()}.
     * @return A {@link List} of {@link CacheableClass}.
     * @deprecated see {@link CacheableListModelLoader}
     */
    @Deprecated
    public static <CacheableClass extends Model> List<CacheableClass> convertToCacheableList(
        Class<CacheableClass> modelClass, Cursor cursor) {
        return convertToCacheableList(modelClass, cursor, FlowManager.getModelAdapter(modelClass).getModelCache());
    }

    /**
     * Loops through a cursor and builds a list of {@link TModel} objects.
     *
     * @param table    The model class that we convert the cursor data into.
     * @param cursor   The cursor from the DB
     * @param <TModel> The class that implements {@link Model}
     * @return An non-null {@link List}
     * @deprecated see {@link ListModelLoader}
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <TModel extends Model> List<TModel> convertToList(Class<TModel> table, Cursor cursor) {
        final List<TModel> entities = new ArrayList<>();
        InstanceAdapter modelAdapter = FlowManager.getInstanceAdapter(table);
        if (modelAdapter != null) {
            // Ensure that we aren't iterating over this cursor concurrently from different threads
            synchronized (cursor) {
                if (cursor.moveToFirst()) {
                    do {
                        Model model = modelAdapter.newInstance();
                        modelAdapter.loadFromCursor(cursor, model);
                        entities.add((TModel) model);
                    }
                    while (cursor.moveToNext());
                }
            }
        }

        return entities;
    }

    /**
     * Notifies the {@link android.database.ContentObserver} that the model has changed.
     *
     * @param action The {@link Action} enum
     * @param table  The table of the model
     */
    public static void notifyModelChanged(Class<? extends Model> table, Action action, Iterable<SQLCondition> sqlConditions) {
        FlowManager.getContext().getContentResolver().notifyChange(getNotificationUri(table, action, sqlConditions), null, true);
    }

    /**
     * Performs necessary logic to notify of {@link Model} changes.
     *
     * @param model          The model to use to notify.
     * @param adapter        The adapter to use thats either a {@link ModelAdapter} or {@link ModelContainerAdapter}
     *                       to handle interactions.
     * @param modelAdapter   The actual {@link ModelAdapter} associated with the {@link ModelClass}/
     * @param action         The {@link Action} that occured.
     * @param <ModelClass>   The original model class.
     * @param <TableClass>   The class of the adapter that we use the model from.
     * @param <AdapterClass> The class of the adapter, which is either a {@link ModelAdapter} or {@link ModelContainerAdapter}
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model, TableClass extends Model, AdapterClass extends RetrievalAdapter & InternalAdapter>
    void notifyModelChanged(TableClass model, AdapterClass adapter, ModelAdapter<ModelClass> modelAdapter, Action action) {
        if (FlowContentObserver.shouldNotify()) {
            notifyModelChanged(modelAdapter.getModelClass(), action,
                adapter.getPrimaryConditionClause(model).getConditions());
        }
    }

    /**
     * Constructs a {@link Uri} from a set of {@link SQLCondition} for specific table.
     *
     * @param modelClass The class of table,
     * @param action     The action to use.
     * @param conditions The set of key-value {@link SQLCondition} to construct into a uri.
     * @return The {@link Uri}.
     */
    public static Uri getNotificationUri(Class<? extends Model> modelClass, Action action, Iterable<SQLCondition> conditions) {
        Uri.Builder uriBuilder = new Uri.Builder().scheme("dbflow")
            .authority(FlowManager.getTableName(modelClass));
        if (action != null) {
            uriBuilder.fragment(action.name());
        }
        if (conditions != null) {
            for (SQLCondition condition : conditions) {
                uriBuilder.appendQueryParameter(Uri.encode(condition.columnName()), Uri.encode(String.valueOf(condition.value())));
            }
        }
        return uriBuilder.build();
    }


    /**
     * Constructs a {@link Uri} from a set of {@link SQLCondition} for specific table.
     *
     * @param modelClass The class of table,
     * @param action     The action to use.
     * @param conditions The set of key-value {@link SQLCondition} to construct into a uri.
     * @return The {@link Uri}.
     */
    public static Uri getNotificationUri(Class<? extends Model> modelClass, Action action, SQLCondition[] conditions) {
        Uri.Builder uriBuilder = new Uri.Builder().scheme("dbflow")
            .authority(FlowManager.getTableName(modelClass));
        if (action != null) {
            uriBuilder.fragment(action.name());
        }
        if (conditions != null && conditions.length > 0) {
            for (SQLCondition condition : conditions) {
                if (condition != null) {
                    uriBuilder.appendQueryParameter(Uri.encode(condition.columnName()), Uri.encode(String.valueOf(condition.value())));
                }
            }
        }
        return uriBuilder.build();
    }

    /**
     * Returns the uri for notifications from model changes
     *
     * @param modelClass  The class to get table from.
     * @param action      the action changed.
     * @param notifyKey   The column key.
     * @param notifyValue The column value that gets turned into a String.
     * @return Notification uri.
     */

    public static Uri getNotificationUri(Class<? extends Model> modelClass, Action action, String notifyKey, Object notifyValue) {
        Condition condition = null;
        if (StringUtils.isNotNullOrEmpty(notifyKey)) {
            condition = Condition.column(new NameAlias.Builder(notifyKey).build()).value(notifyValue);
        }
        return getNotificationUri(modelClass, action, new SQLCondition[]{condition});
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
    public static <ModelClass extends Model> void dropIndex(DatabaseWrapper databaseWrapper, String indexName) {
        QueryBuilder queryBuilder = new QueryBuilder("DROP INDEX IF EXISTS ")
            .append(QueryBuilder.quoteIfNeeded(indexName));
        databaseWrapper.execSQL(queryBuilder.getQuery());
    }

    public static <ModelClass extends Model> void dropIndex(Class<ModelClass> onTable, String indexName) {
        dropIndex(FlowManager.getDatabaseForTable(onTable).getWritableDatabase(), indexName);
    }

    /**
     * Adds {@link ContentValues} to the specified {@link ConditionGroup}.
     *
     * @param contentValues  The content values to convert.
     * @param conditionGroup The group to put them into as {@link Condition}.
     */
    public static void addContentValues(@NonNull ContentValues contentValues, @NonNull ConditionGroup conditionGroup) {
        java.util.Set<Map.Entry<String, Object>> entries = contentValues.valueSet();

        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            conditionGroup.and(Condition.column(new NameAlias.Builder(key).build())
                .is(contentValues.get(key)));
        }
    }

    /**
     * @param contentValues The object to check existence of.
     * @param key           The key to check.
     * @return The key, whether it's quoted or not.
     */
    public static String getContentValuesKey(ContentValues contentValues, String key) {
        String quoted = QueryBuilder.quoteIfNeeded(key);
        if (contentValues.containsKey(quoted)) {
            return quoted;
        } else {
            String stripped = QueryBuilder.stripQuotes(key);
            if (contentValues.containsKey(stripped)) {
                return stripped;
            } else {
                throw new IllegalArgumentException("Could not find the specified key in the Content Values object.");
            }
        }
    }

    public static long longForQuery(DatabaseWrapper wrapper, String query) {
        DatabaseStatement statement = wrapper.compileStatement(query);
        try {
            return statement.simpleQueryForLong();
        } finally {
            statement.close();
        }
    }

    /**
     * Converts a byte[] to a String hex representation for within wrapper queries.
     */
    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}

