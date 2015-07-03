package com.raizlabs.android.dbflow.runtime;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.structure.BaseModel.Action;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description: Listens for {@link Model} changes. Register for specific
 * tables with {@link #addModelChangeListener(FlowContentObserver.OnModelStateChangedListener)}.
 * Provides ability to register and deregister listeners for when data is inserted, deleted, updated, and saved if the device is
 * above {@link VERSION_CODES#JELLY_BEAN}. If below it will only provide one callback.
 */
public class FlowContentObserver extends ContentObserver {

    private static final List<FlowContentObserver> OBSERVER_LIST = new ArrayList<>();
    private static boolean forceNotify = false;

    /**
     * @return true if we have registered for content changes. Otherwise we do not notify
     * in {@link SqlUtils}
     * for efficiency purposes.
     */
    public static boolean shouldNotify() {
        return forceNotify || !OBSERVER_LIST.isEmpty();
    }

    /**
     * @param forceNotify if true, this will force itself to notify whenever a model changes even though
     *                    an observer (appears to be) is not registered.
     */
    public static void setShouldForceNotify(boolean forceNotify) {
        FlowContentObserver.forceNotify = forceNotify;
    }

    /**
     * Called when the {@link FlowContentObserver} receives a URI from a model change event. This event
     * is generalized to not include specific model keys. To get more specific events, register a
     * {@link OnSpecificModelStateChangedListener}.
     */
    public interface OnModelStateChangedListener {

        /**
         * Notifies that the state of a {@link Model}
         * has changed for the table this is registered for.
         *
         * @param table  The table that this change occurred on. This is ONLY available on {@link VERSION_CODES#JELLY_BEAN}
         *               and up.
         * @param action The action on the model. for versions prior to {@link VERSION_CODES#JELLY_BEAN} ,
         *               the {@link Action#CHANGE} will always be called for any action.
         */
        void onModelStateChanged(Class<? extends Model> table, Action action);
    }

    /**
     * Listens for specific model changes. This is only available in {@link VERSION_CODES#JELLY_BEAN}
     * or higher due to the api of {@link ContentObserver}.
     */
    @TargetApi(VERSION_CODES.JELLY_BEAN)
    public interface OnSpecificModelStateChangedListener {

        /**
         * Notifies that the state of a {@link Model}
         * has changed for the table this is registered for.
         *
         * @param table      The table that this change occurred on. This is ONLY available on {@link VERSION_CODES#JELLY_BEAN}
         *                   and up.
         * @param action     The action on the model. for versions prior to {@link VERSION_CODES#JELLY_BEAN} ,
         *                   the {@link Action#CHANGE} will always be called for any action.
         * @param columnName The name of the primary column with the id that's changed.
         * @param value      the value from the primary key thats changed converted to String.
         */
        void onModelStateChanged(Class<? extends Model> table, Action action, String columnName, String value);
    }

    private final List<OnModelStateChangedListener> modelChangeListeners = new ArrayList<>();
    private final List<OnSpecificModelStateChangedListener> specificModelChangeListeners = new ArrayList<>();
    private final Map<String, Class<? extends Model>> registeredTables = new HashMap<>();
    private final Set<Uri> notificationUris = new HashSet<>();

    protected boolean isInTransaction = false;
    private boolean notifyAllUris = false;

    public FlowContentObserver() {
        super(null);
    }

    public FlowContentObserver(Handler handler) {
        super(handler);
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
                        onChange(true, uri);
                    }
                    notificationUris.clear();
                }
            }
        }
    }

    /**
     * Add a listener for model changes
     *
     * @param modelChangeListener Generic model change events from an {@link Action}
     */
    public void addModelChangeListener(OnModelStateChangedListener modelChangeListener) {
        modelChangeListeners.add(modelChangeListener);
    }

    /**
     * Removes a listener for model changes
     *
     * @param modelChangeListener Generic model change events from a {@link Action}
     */
    public void removeModelChangeListener(OnModelStateChangedListener modelChangeListener) {
        modelChangeListeners.remove(modelChangeListener);
    }

    /**
     * Add a specific listener for model changes
     *
     * @param modelChangeListener Listens for specific model change events.
     */
    public void addSpecificModelChangeListener(OnSpecificModelStateChangedListener modelChangeListener) {
        specificModelChangeListeners.add(modelChangeListener);
    }

    /**
     * Removes a specific listener for model changes
     *
     * @param modelChangeListener Listens for specific model change events.
     */
    public void removeSpecificModelChangeListener(OnSpecificModelStateChangedListener modelChangeListener) {
        specificModelChangeListeners.remove(modelChangeListener);
    }

    /**
     * Registers the observer for model change events for specific class.
     */
    public void registerForContentChanges(Context context, Class<? extends Model> table) {
        context.getContentResolver().registerContentObserver(SqlUtils.getNotificationUri(table, null), true, this);
        if (!OBSERVER_LIST.contains(this)) {
            OBSERVER_LIST.add(this);
        }
        if (!registeredTables.containsValue(table)) {
            registeredTables.put(FlowManager.getTableName(table), table);
        }
    }

    /**
     * Unregisters this list for model change events
     */
    public void unregisterForContentChanges(Context context) {
        context.getContentResolver().unregisterContentObserver(this);
        OBSERVER_LIST.remove(this);
        registeredTables.clear();
    }

    @Override
    public void onChange(boolean selfChange) {
        for (OnModelStateChangedListener modelChangeListener : modelChangeListeners) {
            modelChangeListener.onModelStateChanged(null, Action.CHANGE);
        }
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        String fragment = uri.getFragment();
        String tableName = uri.getAuthority();

        String columnName = null;
        String param = null;

        Set<String> queryNames = uri.getQueryParameterNames();
        if (!queryNames.isEmpty()) {
            for (String key : queryNames) {
                // for now we get first key we find
                // maybe in future we add multi-column support
                param = Uri.decode(uri.getQueryParameter(key));
                columnName = key;
                break;
            }
        }

        Class<? extends Model> table = registeredTables.get(tableName);
        if (!isInTransaction) {

            Action action = Action.valueOf(fragment);
            if (action != null) {
                for (OnModelStateChangedListener modelChangeListener : modelChangeListeners) {
                    modelChangeListener.onModelStateChanged(table, action);
                }

                if (columnName != null && param != null) {
                    for (OnSpecificModelStateChangedListener modelChangeListener : specificModelChangeListeners) {
                        modelChangeListener.onModelStateChanged(table, action, columnName, param);
                    }
                }
            }
        } else {
            // convert this uri to a CHANGE op if we don't care about individual changes.
            if (!notifyAllUris) {
                uri = SqlUtils.getNotificationUri(table, Action.CHANGE);
            }
            synchronized (notificationUris) {
                // add and keep track of unique notification uris for when transaction completes.
                notificationUris.add(uri);
            }
        }
    }

}
