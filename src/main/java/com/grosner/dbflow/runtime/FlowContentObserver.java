package com.grosner.dbflow.runtime;

import android.annotation.TargetApi;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.SqlUtils;
import com.grosner.dbflow.structure.BaseModel;
import com.grosner.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Description: Listens for {@link com.grosner.dbflow.structure.Model} changes. Register for specific
 * tables with {@link #addModelChangeListener(com.grosner.dbflow.runtime.FlowContentObserver.ModelChangeListener)}.
 * Provides ability to register and deregister listeners for when data is inserted, deleted, updated, and saved if the device is
 * above {@link android.os.Build.VERSION_CODES#JELLY_BEAN}. If below it will only provide one callback.
 */
public class FlowContentObserver extends ContentObserver {

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
    public void registerForContentChanges(Class<? extends Model> table) {
        FlowManager.getContext().getContentResolver().registerContentObserver(SqlUtils.getNotificationUri(table, null), true, this);
    }

    /**
     * Unregisters this list for model change events
     */
    public void unregisterForContentChanges() {
        FlowManager.getContext().getContentResolver().unregisterContentObserver(this);
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
}
