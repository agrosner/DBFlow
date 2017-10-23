package com.raizlabs.android.dbflow.sql;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.StringUtils;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.NotifyDistributor;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.Operator;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;
import com.raizlabs.android.dbflow.sql.language.SQLOperator;
import com.raizlabs.android.dbflow.structure.BaseModel.Action;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.Map;

/**
 * Description: Provides some handy methods for dealing with SQL statements. It's purpose is to move the
 * methods away from the {@link Model} class and let any class use these.
 */
public class SqlUtils {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Notifies the {@link ContentObserver} that the model has changed.
     */
    @Deprecated
    public static void notifyModelChanged(Class<?> table, Action action,
                                          Iterable<SQLOperator> sqlOperators) {
        FlowManager.getContext().getContentResolver().notifyChange(
            getNotificationUri(table, action, sqlOperators), null, true);
    }

    /**
     * Performs necessary logic to notify of {@link Model} changes.
     *
     * @see NotifyDistributor
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <TModel> void notifyModelChanged(@Nullable TModel model,
                                                   @NonNull ModelAdapter<TModel> modelAdapter,
                                                   @NonNull Action action) {
        NotifyDistributor.Companion.get().notifyModelChanged(model, modelAdapter, action);
    }

    /**
     * Notifies listeners of table-level changes from the SQLite-wrapper language.
     *
     * @see NotifyDistributor
     */
    @Deprecated
    public static <TModel> void notifyTableChanged(@NonNull Class<TModel> table,
                                                   @NonNull Action action) {
        NotifyDistributor.Companion.get().notifyTableChanged(table, action);
    }

    /**
     * Constructs a {@link Uri} from a set of {@link SQLOperator} for specific table.
     *
     * @param modelClass The class of table,
     * @param action     The action to use.
     * @param conditions The set of key-value {@link SQLOperator} to construct into a uri.
     * @return The {@link Uri}.
     */
    public static Uri getNotificationUri(@NonNull Class<?> modelClass,
                                         @Nullable Action action,
                                         @Nullable Iterable<SQLOperator> conditions) {
        Uri.Builder uriBuilder = new Uri.Builder().scheme("dbflow")
            .authority(FlowManager.getTableName(modelClass));
        if (action != null) {
            uriBuilder.fragment(action.name());
        }
        if (conditions != null) {
            for (SQLOperator condition : conditions) {
                uriBuilder.appendQueryParameter(Uri.encode(condition.columnName()), Uri.encode(String.valueOf(condition.value())));
            }
        }
        return uriBuilder.build();
    }


    /**
     * Constructs a {@link Uri} from a set of {@link SQLOperator} for specific table.
     *
     * @param modelClass The class of table,
     * @param action     The action to use.
     * @param conditions The set of key-value {@link SQLOperator} to construct into a uri.
     * @return The {@link Uri}.
     */
    public static Uri getNotificationUri(@NonNull Class<?> modelClass,
                                         @NonNull Action action,
                                         @Nullable SQLOperator[] conditions) {
        Uri.Builder uriBuilder = new Uri.Builder().scheme("dbflow")
            .authority(FlowManager.getTableName(modelClass));
        if (action != null) {
            uriBuilder.fragment(action.name());
        }
        if (conditions != null && conditions.length > 0) {
            for (SQLOperator condition : conditions) {
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

    public static Uri getNotificationUri(@NonNull Class<?> modelClass,
                                         @NonNull Action action,
                                         @NonNull String notifyKey,
                                         @Nullable Object notifyValue) {
        Operator operator = null;
        if (StringUtils.isNotNullOrEmpty(notifyKey)) {
            operator = Operator.op(new NameAlias.Builder(notifyKey).build()).value(notifyValue);
        }
        return getNotificationUri(modelClass, action, new SQLOperator[]{operator});
    }

    /**
     * @param modelClass The model class to use.
     * @param action     The {@link Action} to use.
     * @return The uri for updates to {@link Model}, meant for general changes.
     */
    public static Uri getNotificationUri(@NonNull Class<?> modelClass, @NonNull Action action) {
        return getNotificationUri(modelClass, action, null, null);
    }


    /**
     * Drops an active TRIGGER by specifying the onTable and triggerName
     *
     * @param mOnTable    The table that this trigger runs on
     * @param triggerName The name of the trigger
     */
    public static void dropTrigger(@NonNull Class<?> mOnTable, @NonNull String triggerName) {
        QueryBuilder queryBuilder = new QueryBuilder("DROP TRIGGER IF EXISTS ")
            .append(triggerName);
        FlowManager.getDatabaseForTable(mOnTable).getWritableDatabase().execSQL(queryBuilder.getQuery());
    }

    /**
     * Drops an active INDEX by specifying the onTable and indexName
     *
     * @param indexName The name of the index.
     */
    public static void dropIndex(@NonNull DatabaseWrapper databaseWrapper,
                                 @NonNull String indexName) {
        QueryBuilder queryBuilder = new QueryBuilder("DROP INDEX IF EXISTS ")
            .append(QueryBuilder.quoteIfNeeded(indexName));
        databaseWrapper.execSQL(queryBuilder.getQuery());
    }

    public static void dropIndex(@NonNull Class<?> onTable,
                                 @NonNull String indexName) {
        dropIndex(FlowManager.getDatabaseForTable(onTable).getWritableDatabase(), indexName);
    }

    /**
     * Adds {@link ContentValues} to the specified {@link OperatorGroup}.
     *
     * @param contentValues The content values to convert.
     * @param operatorGroup The group to put them into as {@link Operator}.
     */
    public static void addContentValues(@NonNull ContentValues contentValues, @NonNull OperatorGroup operatorGroup) {
        java.util.Set<Map.Entry<String, Object>> entries = contentValues.valueSet();

        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            operatorGroup.and(Operator.op(new NameAlias.Builder(key).build())
                .is(contentValues.get(key)));
        }
    }

    /**
     * @param contentValues The object to check existence of.
     * @param key           The key to check.
     * @return The key, whether it's quoted or not.
     */
    @NonNull
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

    public static long longForQuery(@NonNull DatabaseWrapper wrapper,
                                    @NonNull String query) {
        DatabaseStatement statement = wrapper.compileStatement(query);
        try {
            return statement.simpleQueryForLong();
        } finally {
            statement.close();
        }
    }

    public static double doubleForQuery(@NonNull DatabaseWrapper wrapper,
                                    @NonNull String query) {
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
    @NonNull
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

