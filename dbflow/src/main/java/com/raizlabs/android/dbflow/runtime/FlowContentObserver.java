package com.raizlabs.android.dbflow.runtime;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.Operator;
import com.raizlabs.android.dbflow.sql.language.SQLOperator;
import com.raizlabs.android.dbflow.structure.BaseModel.Action;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description: Listens for {@link Model} changes. Register for specific
 * tables with {@link #addModelChangeListener(FlowContentObserver.OnModelStateChangedListener)}.
 * Provides ability to register and deregister listeners for when data is inserted, deleted, updated, and saved if the device is
 * above {@link VERSION_CODES#JELLY_BEAN}. If below it will only provide one callback. This is to be paired
 * with the {@link ContentResolverNotifier} specified in the {@link DatabaseConfig} by default.
 */
public class FlowContentObserver extends ContentObserver {

    private static final AtomicInteger REGISTERED_COUNT = new AtomicInteger(0);
    private static boolean forceNotify = false;
    @NonNull private final String contentAuthority;

    /**
     * @return true if we have registered for content changes. Otherwise we do not notify
     * in {@link SqlUtils}
     * for efficiency purposes.
     */
    public static boolean shouldNotify() {
        return forceNotify || REGISTERED_COUNT.get() > 0;
    }

    /**
     * Removes count of observers registered, so we do not send out calls when {@link Model} changes.
     */
    public static void clearRegisteredObserverCount() {
        REGISTERED_COUNT.set(0);
    }

    /**
     * @param forceNotify if true, this will force itself to notify whenever a model changes even though
     *                    an observer (appears to be) is not registered.
     */
    public static void setShouldForceNotify(boolean forceNotify) {
        FlowContentObserver.forceNotify = forceNotify;
    }

    /**
     * Listens for specific model changes. This is only available in {@link VERSION_CODES#JELLY_BEAN}
     * or higher due to the api of {@link ContentObserver}.
     */
    public interface OnModelStateChangedListener {

        /**
         * Notifies that the state of a {@link Model}
         * has changed for the table this is registered for. This method can be called from the thread another then main thread.
         *
         * @param table            The table that this change occurred on. This is ONLY available on {@link VERSION_CODES#JELLY_BEAN}
         *                         and up.
         * @param action           The action on the model. for versions prior to {@link VERSION_CODES#JELLY_BEAN} ,
         *                         the {@link Action#CHANGE} will always be called for any action.
         * @param primaryKeyValues The array of primary {@link SQLOperator} of what changed. Call {@link SQLOperator#columnName()}
         *                         and {@link SQLOperator#value()} to get each information.
         */
        void onModelStateChanged(@Nullable Class<?> table, Action action, @NonNull SQLOperator[] primaryKeyValues);
    }

    public interface ContentChangeListener extends OnModelStateChangedListener, OnTableChangedListener {

    }

    private final Set<OnModelStateChangedListener> modelChangeListeners = new CopyOnWriteArraySet<>();
    private final Set<OnTableChangedListener> onTableChangedListeners = new CopyOnWriteArraySet<>();
    private final Map<String, Class<?>> registeredTables = new HashMap<>();
    private final Set<Uri> notificationUris = new HashSet<>();
    private final Set<Uri> tableUris = new HashSet<>();

    protected boolean isInTransaction = false;
    private boolean notifyAllUris = false;

    public FlowContentObserver(@NonNull String contentAuthority) {
        super(null);
        this.contentAuthority = contentAuthority;
    }

    public FlowContentObserver(@Nullable Handler handler, @NonNull String contentAuthority) {
        super(handler);
        this.contentAuthority = contentAuthority;
    }

    /**
     * If true, this class will get specific when it needs to, such as using all {@link Action} qualifiers.
     * If false, it only uses the {@link Action#CHANGE} action in callbacks.
     *
     * @param notifyAllUris
     */
    public void setNotifyAllUris(boolean notifyAllUris) {
        this.notifyAllUris = notifyAllUris;
    }

    /**
     * Starts a transaction where when it is finished, this class will receive a notification of all of the changes by
     * calling {@link #endTransactionAndNotify()}. Note it may lead to unexpected behavior if called from different threads.
     */
    public void beginTransaction() {
        if (!isInTransaction) {
            isInTransaction = true;
        }
    }

    /**
     * Ends the transaction where it finishes, and will call {@link #onChange(boolean, Uri)} for Jelly Bean and up for
     * every URI called (if set), or {@link #onChange(boolean)} once for lower than Jelly bean.
     */
    public void endTransactionAndNotify() {
        if (isInTransaction) {
            isInTransaction = false;

            if (Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
                onChange(true);
            } else {
                synchronized (notificationUris) {
                    for (Uri uri : notificationUris) {
                        onChange(true, uri, true);
                    }
                    notificationUris.clear();
                }
                synchronized (tableUris) {
                    for (Uri uri : tableUris) {
                        for (OnTableChangedListener onTableChangedListener : onTableChangedListeners) {
                            onTableChangedListener.onTableChanged(registeredTables.get(uri.getAuthority()),
                                    Action.valueOf(uri.getFragment()));
                        }
                    }
                    tableUris.clear();
                }
            }
        }
    }

    /**
     * Add a listener for model changes
     *
     * @param modelChangeListener Generic model change events from an {@link Action}
     */
    public void addModelChangeListener(@NonNull OnModelStateChangedListener modelChangeListener) {
        modelChangeListeners.add(modelChangeListener);
    }

    /**
     * Removes a listener for model changes
     *
     * @param modelChangeListener Generic model change events from a {@link Action}
     */
    public void removeModelChangeListener(@NonNull OnModelStateChangedListener modelChangeListener) {
        modelChangeListeners.remove(modelChangeListener);
    }

    public void addOnTableChangedListener(@NonNull OnTableChangedListener onTableChangedListener) {
        onTableChangedListeners.add(onTableChangedListener);
    }

    public void removeTableChangedListener(@NonNull OnTableChangedListener onTableChangedListener) {
        onTableChangedListeners.remove(onTableChangedListener);
    }

    /**
     * Add a listener for model + table changes
     *
     * @param contentChangeListener Generic model change events from an {@link Action}
     */
    public void addContentChangeListener(@NonNull ContentChangeListener contentChangeListener) {
        modelChangeListeners.add(contentChangeListener);
        onTableChangedListeners.add(contentChangeListener);
    }

    /**
     * Removes a listener for model + table changes
     *
     * @param contentChangeListener Generic model change events from a {@link Action}
     */
    public void removeContentChangeListener(@NonNull ContentChangeListener contentChangeListener) {
        modelChangeListeners.remove(contentChangeListener);
        onTableChangedListeners.remove(contentChangeListener);
    }

    /**
     * Registers the observer for model change events for specific class.
     */
    public void registerForContentChanges(@NonNull Context context,
                                          @NonNull Class<?> table) {
        registerForContentChanges(context.getContentResolver(), table);
    }

    /**
     * Registers the observer for model change events for specific class.
     */
    public void registerForContentChanges(@NonNull ContentResolver contentResolver,
                                          @NonNull Class<?> table) {
        contentResolver.registerContentObserver(
                SqlUtils.getNotificationUri(contentAuthority, table, null), true, this);
        REGISTERED_COUNT.incrementAndGet();
        if (!registeredTables.containsValue(table)) {
            registeredTables.put(FlowManager.getTableName(table), table);
        }
    }

    /**
     * Unregisters this list for model change events
     */
    public void unregisterForContentChanges(@NonNull Context context) {
        context.getContentResolver().unregisterContentObserver(this);
        REGISTERED_COUNT.decrementAndGet();
        registeredTables.clear();
    }

    public boolean isSubscribed() {
        return !registeredTables.isEmpty();
    }

    @Override
    public void onChange(boolean selfChange) {
        for (OnModelStateChangedListener modelChangeListener : modelChangeListeners) {
            modelChangeListener.onModelStateChanged(null, Action.CHANGE, new SQLOperator[0]);
        }

        for (OnTableChangedListener onTableChangedListener : onTableChangedListeners) {
            onTableChangedListener.onTableChanged(null, Action.CHANGE);
        }
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        onChange(selfChange, uri, false);
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    private void onChange(boolean selfChanges, Uri uri, boolean calledInternally) {
        String fragment = uri.getFragment();
        String tableName = uri.getQueryParameter(SqlUtils.TABLE_QUERY_PARAM);

        String columnName;
        String param;

        Set<String> queryNames = uri.getQueryParameterNames();
        SQLOperator[] columnsChanged = new SQLOperator[queryNames.size() - 1];
        if (!queryNames.isEmpty()) {
            int index = 0;
            for (String key : queryNames) {
                if (!SqlUtils.TABLE_QUERY_PARAM.equals(key)) {
                    param = Uri.decode(uri.getQueryParameter(key));
                    columnName = Uri.decode(key);
                    columnsChanged[index] = Operator.op(new NameAlias.Builder(columnName).build())
                            .eq(param);
                    index++;
                }
            }
        }

        Class<?> table = registeredTables.get(tableName);
        Action action = Action.valueOf(fragment);
        if (!isInTransaction) {

            for (OnModelStateChangedListener modelChangeListener : modelChangeListeners) {
                modelChangeListener.onModelStateChanged(table, action, columnsChanged);
            }

            if (!calledInternally) {
                for (OnTableChangedListener onTableChangeListener : onTableChangedListeners) {
                    onTableChangeListener.onTableChanged(table, action);
                }
            }
        } else {
            // convert this uri to a CHANGE op if we don't care about individual changes.
            if (!notifyAllUris) {
                action = Action.CHANGE;
                uri = SqlUtils.getNotificationUri(contentAuthority, table, action);
            }
            synchronized (notificationUris) {
                // add and keep track of unique notification uris for when transaction completes.
                notificationUris.add(uri);
            }

            synchronized (tableUris) {
                tableUris.add(SqlUtils.getNotificationUri(contentAuthority, table, action));
            }
        }
    }

}
