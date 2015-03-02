package com.raizlabs.android.dbflow.runtime;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;

import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Description: Listens for {@link com.raizlabs.android.dbflow.structure.Model} changes. Register for specific
 * tables with {@link #addModelChangeListener(com.raizlabs.android.dbflow.runtime.FlowContentObserver.ModelChangeListener)}.
 * Provides ability to register and deregister listeners for when data is inserted, deleted, updated, and saved if the device is
 * above {@link android.os.Build.VERSION_CODES#JELLY_BEAN}. If below it will only provide one callback.
 */
public class FlowContentObserver extends ContentObserver {

    private static List<FlowContentObserver> mObserverList = new ArrayList<>();

    private static boolean forceNotify = false;

    /**
     * @return true if we have registered for content changes. Otherwise we do not notify
     * in {@link com.raizlabs.android.dbflow.sql.SqlUtils#notifyModelChanged(Class, com.raizlabs.android.dbflow.structure.BaseModel.Action)}
     * for efficiency purposes.
     */
    public static boolean shouldNotify() {
        return forceNotify || !mObserverList.isEmpty();
    }

    /**
     * @param forceNotify if true, this will force itself to notify whenever a model changes even though
     *                    an observer (appears to be) is not registered.
     */
    public static void setShouldForceNotify(boolean forceNotify) {
        FlowContentObserver.forceNotify = forceNotify;
    }

    /**
     * Listeners for model changes.
     */
    private List<ModelChangeListener> mModelChangeListeners;


    public FlowContentObserver() {
        super(null);
        mModelChangeListeners = new ArrayList<ModelChangeListener>();
    }

    /**
     * Add a listener for model changes
     *
     * @param modelChangeListener
     */
    public void addModelChangeListener(ModelChangeListener modelChangeListener) {
        mModelChangeListeners.add(modelChangeListener);
    }

    /**
     * Removes a listener for model changes
     *
     * @param modelChangeListener
     */
    public void removeModelChangeListener(ModelChangeListener modelChangeListener) {
        mModelChangeListeners.remove(modelChangeListener);
    }

    /**
     * Registers the observer for model change events for specific class.
     */
    public void registerForContentChanges(Context context, Class<? extends Model> table) {
        context.getContentResolver().registerContentObserver(SqlUtils.getNotificationUri(table, null), true, this);
        mObserverList.add(this);
    }

    /**
     * Unregisters this list for model change events
     */
    public void unregisterForContentChanges(Context context) {
        context.getContentResolver().unregisterContentObserver(this);
        mObserverList.remove(this);
    }

    @Override
    public void onChange(boolean selfChange) {
        for (ModelChangeListener modelChangeListener : mModelChangeListeners) {
            modelChangeListener.onModelChanged();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        String fragment = uri.getFragment();
        BaseModel.Action action = BaseModel.Action.valueOf(fragment);
        if (action != null) {
            for (ModelChangeListener modelChangeListener : mModelChangeListeners) {
                if (action.equals(BaseModel.Action.DELETE)) {
                    modelChangeListener.onModelDeleted();
                } else if (action.equals(BaseModel.Action.INSERT)) {
                    modelChangeListener.onModelInserted();
                } else if (action.equals(BaseModel.Action.UPDATE)) {
                    modelChangeListener.onModelUpdated();
                } else if (action.equals(BaseModel.Action.SAVE)) {
                    modelChangeListener.onModelSaved();
                }
            }
        }
    }

    /**
     * Callback for when models are saved, deleted, inserted, or updated. Note: the methods will only work
     * for devices {@link android.os.Build.VERSION_CODES#JELLY_BEAN} or up. Otherwise the singular method,
     * {@link #onModelChanged()} will be called.
     */
    public interface ModelChangeListener {

        /**
         * Called when the model has changed. This is only called in versions below {@link android.os.Build.VERSION_CODES#JELLY_BEAN_MR1}.
         */
        public void onModelChanged();

        /**
         * Called when the model has been saved. This is only available to {@link android.os.Build.VERSION_CODES#JELLY_BEAN_MR1}.
         */
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public void onModelSaved();

        /**
         * Called when model has been deleted. This is only available to {@link android.os.Build.VERSION_CODES#JELLY_BEAN_MR1}.
         */
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public void onModelDeleted();

        /**
         * Called when the model has been inserted. This is only available to {@link android.os.Build.VERSION_CODES#JELLY_BEAN_MR1}.
         */
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public void onModelInserted();

        /**
         * Called when the model has been updated. This is only available to {@link android.os.Build.VERSION_CODES#JELLY_BEAN_MR1}.
         */
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public void onModelUpdated();
    }

    /**
     * Provides default implementation of the {@link com.raizlabs.android.dbflow.runtime.FlowContentObserver.ModelChangeListener}
     * enabling you to only have to implement a small subset of methods.
     */
    public static class ModelChangeListenerAdapter implements ModelChangeListener {

        @Override
        public void onModelChanged() {
            onModelStateChanged(BaseModel.Action.CHANGE);
        }

        @Override
        public void onModelSaved() {
            onModelStateChanged(BaseModel.Action.SAVE);
        }

        @Override
        public void onModelDeleted() {
            onModelStateChanged(BaseModel.Action.DELETE);
        }

        @Override
        public void onModelInserted() {
            onModelStateChanged(BaseModel.Action.INSERT);
        }

        @Override
        public void onModelUpdated() {
            onModelStateChanged(BaseModel.Action.UPDATE);
        }

        /**
         * Called for all versions of devices, will strictly notify that the state of a {@link com.raizlabs.android.dbflow.structure.Model}
         * has changed.
         *
         * @param action The action on the model. for versions prior to {@link android.os.Build.VERSION_CODES#JELLY_BEAN_MR1} ,
         *               the {@link com.raizlabs.android.dbflow.structure.BaseModel.Action#CHANGE} will always be called for any action.
         */
        public void onModelStateChanged(BaseModel.Action action) {

        }
    }
}
