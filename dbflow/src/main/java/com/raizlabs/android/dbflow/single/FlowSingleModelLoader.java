package com.raizlabs.android.dbflow.single;

import android.annotation.TargetApi;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.HashSet;

/**
 * Abstract class for all DBFlow Loader classes that return a single
 * model element.
 *
 * @param <TModel>
 */
@TargetApi(11)
public abstract class FlowSingleModelLoader <TModel extends Model>
    extends AsyncTaskLoader<TModel> {
    /// Model type being loaded.
    private final Class<TModel> model;

    /// Adapter for converting cursor into target model.
    private final InstanceAdapter<TModel> adapter;

    /// Queriable operation that the loader executes.
    private Queriable queriable;

    /// Cursor for the loader.
    private TModel result;

    /// Observe changes to the model.
    private boolean observeModel = true;

    /// Collection of models to be observed.
    private final HashSet<Class<? extends Model>> mModels = new HashSet<>();

    private class ForceLoadContentObserver extends FlowContentObserver {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            onContentChanged();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            onContentChanged();
        }
    }

    private final FlowContentObserver mObserver = new ForceLoadContentObserver();

    protected FlowSingleModelLoader(Context context, Class<TModel> model, InstanceAdapter<TModel> adapter, Queriable queriable) {
        super(context);

        this.queriable = queriable;
        this.model = model;
        this.adapter = adapter;
    }

    /* Runs on a worker thread */
    @Override
    public TModel loadInBackground() {
        Cursor cursor = this.queriable.query();

        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }

        TModel model = this.adapter.newInstance();
        this.adapter.loadFromCursor(cursor, model);

        return model;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(TModel result) {
        this.result = result;

        if (this.isStarted()) {
            super.deliverResult(result);
        }
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid the
     * result may be passed to the callbacks immediately.
     * <p/>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (result != null) {
            this.deliverResult(result);
        }

        // Start watching for changes to the model.
        if (this.observeModel) {
            this.registerForContentChanges(this.model);
        }

        if (this.takeContentChanged() || this.result == null) {
            this.forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        this.cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        this.onStopLoading();

        if (this.result != null) {
            this.result = null;
        }

        // Unregister the loader for content changes.
        this.mObserver.unregisterForContentChanges(this.getContext());
    }

    public Class<TModel> getModel() {
        return this.model;
    }

    public void setObserveModel(boolean observeModel) {
        this.observeModel = observeModel;
    }

    public void registerForContentChanges(Class<? extends Model> model) {
        if (this.mModels.contains(model)) {
            return;
        }

        this.mModels.add(model);
        this.mObserver.registerForContentChanges(this.getContext(), model);
    }
}