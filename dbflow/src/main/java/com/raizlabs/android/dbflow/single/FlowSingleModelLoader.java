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
    private final Class<TModel> mModel;

    /// Adapter for converting cursor into target model.
    private final InstanceAdapter<TModel> mAdapter;

    /// Queriable operation that the loader executes.
    private Queriable mQueriable;

    /// Cursor for the loader.
    private TModel mResult;

    /// Observe changes to the model.
    private boolean mObserveModel = true;

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

        this.mQueriable = queriable;
        this.mModel = model;
        this.mAdapter = adapter;
    }

    /* Runs on a worker thread */
    @Override
    public TModel loadInBackground() {
        Cursor cursor = this.mQueriable.query();

        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }

        TModel model = this.mAdapter.newInstance();
        this.mAdapter.loadFromCursor(cursor, model);

        return model;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(TModel result) {
        this.mResult = result;

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
        if (mResult != null) {
            this.deliverResult(mResult);
        }

        // Start watching for changes to the model.
        if (this.mObserveModel) {
            this.registerForContentChanges(this.mModel);
        }

        if (this.takeContentChanged() || this.mResult == null) {
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

        if (this.mResult != null) {
            this.mResult = null;
        }

        // Unregister the loader for content changes.
        this.mObserver.unregisterForContentChanges(this.getContext());
    }

    public Class<TModel> getModel() {
        return this.mModel;
    }

    public void setObserveModel(boolean observeModel) {
        this.mObserveModel = observeModel;
    }

    public void registerForContentChanges(Class<? extends Model> model) {
        if (this.mModels.contains(model)) {
            return;
        }

        this.mModels.add(model);
        this.mObserver.registerForContentChanges(this.getContext(), model);
    }
}