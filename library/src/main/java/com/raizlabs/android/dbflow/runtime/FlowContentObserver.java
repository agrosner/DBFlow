package com.raizlabs.android.dbflow.runtime;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: Listens for {@link com.raizlabs.android.dbflow.structure.Model} changes. Register for specific
 * tables with {@link #addModelChangeListener(com.raizlabs.android.dbflow.runtime.FlowContentObserver.OnModelStateChangedListener)}.
 * Provides ability to register and deregister listeners for when data is inserted, deleted, updated, and saved if the device is
 * above {@link android.os.Build.VERSION_CODES#JELLY_BEAN}. If below it will only provide one callback.
 */
public class FlowContentObserver extends ContentObserver {

    private static final List<FlowContentObserver> OBSERVER_LIST = new ArrayList<>();

    private static boolean forceNotify = false;

    /**
     * @return true if we have registered for content changes. Otherwise we do not notify
     * in {@link com.raizlabs.android.dbflow.sql.SqlUtils#notifyModelChanged(Class, com.raizlabs.android.dbflow.structure.BaseModel.Action)}
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
     * Provides default implementation of the {@link com.raizlabs.android.dbflow.runtime.FlowContentObserver.OnModelStateChangedListener}
     * enabling you to only have to implement a small subset of methods.
     */
    public interface OnModelStateChangedListener {

        /**
         * Notifies that the state of a {@link com.raizlabs.android.dbflow.structure.Model}
         * has changed for the table this is registered for.
         *
         * @param action The action on the model. for versions prior to {@link android.os.Build.VERSION_CODES#JELLY_BEAN_MR1} ,
         *               the {@link com.raizlabs.android.dbflow.structure.BaseModel.Action#CHANGE} will always be called for any action.
         */
        void onModelStateChanged(Class<? extends Model> table, BaseModel.Action action);
    }

    /**
     * Listeners for model changes.
     */
    private final List<OnModelStateChangedListener> modelChangeListeners = new ArrayList<>();

    private final Map<String, Class<? extends Model>> registeredTables = new HashMap<>();

    public FlowContentObserver() {
        super(null);
    }

    /**
     * Add a listener for model changes
     *
     * @param modelChangeListener
     */
    public void addModelChangeListener(OnModelStateChangedListener modelChangeListener) {
        modelChangeListeners.add(modelChangeListener);
    }

    /**
     * Removes a listener for model changes
     *
     * @param modelChangeListener
     */
    public void removeModelChangeListener(OnModelStateChangedListener modelChangeListener) {
        modelChangeListeners.remove(modelChangeListener);
    }

    /**
     * Registers the observer for model change events for specific class.
     */
    public void registerForContentChanges(Context context, Class<? extends Model> table) {
        context.getContentResolver().registerContentObserver(SqlUtils.getNotificationUri(table, null), true, this);
        if (!OBSERVER_LIST.contains(this)) {
            OBSERVER_LIST.add(this);
        }
        if(!registeredTables.containsValue(table)) {
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
            modelChangeListener.onModelStateChanged(null, BaseModel.Action.CHANGE);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        String fragment = uri.getFragment();
        String tableName = uri.getEncodedSchemeSpecificPart().replace("//","");
        BaseModel.Action action = BaseModel.Action.valueOf(fragment);
        if (action != null) {
            for (OnModelStateChangedListener modelChangeListener : modelChangeListeners) {
                modelChangeListener.onModelStateChanged(registeredTables.get(tableName), action);
            }
        }
    }

}
