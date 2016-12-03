package com.raizlabs.android.dbflow.list;

import android.annotation.TargetApi;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.Collection;
import java.util.HashSet;

/**
 * Specialization of AsyncTaskLoader for Cursor objects in DBFlow.
 */
@TargetApi(11)
public class FlowCursorLoader extends AsyncTaskLoader<Cursor> {
    /// Models to be observed for changes.
    private final HashSet<Class<? extends Model>> models = new HashSet<>();

    /// Queriable operation that the loader executes.
    private Queriable queriable;

    /// Cursor for the loader.
    private Cursor cursor;

    /// The observer that triggers the loader to reload anytime if it receives
    /// notification of a change.
    private final ForceLoadContentObserver observer = new ForceLoadContentObserver();

    private boolean listening = false;

    /**
     * Creates a fully-specified CursorLoader.  See {@link android.content.ContentResolver#query(Uri,
     * String[], String, String[], String) ContentResolver.query()} for documentation on the meaning
     * of the parameters.  These will be passed as-is to that call.
     */
    public FlowCursorLoader(Context context, Queriable queriable) {
        super(context);

        this.queriable = queriable;
    }

    @Override
    public Cursor loadInBackground() {
        Cursor cursor = this.queriable.query();

        if (cursor != null) {
            cursor.getCount();
        }

        return cursor;
    }

    @Override
    public void deliverResult(Cursor cursor) {
        if (this.isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }

            return;
        }

        Cursor oldCursor = this.cursor;
        this.cursor = cursor;

        if (this.isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }

        // Now that the result has been delivered, start listening for changes
        // to the target models. Doing this at anytime earlier runs the risk of
        // listening for changes while we are still loading content.
        this.startListeningForChanges();
    }

    /**
     * Register the loader for changes to a Flow model. When changes to the model are
     * detected, then the loader will automatically reload the content.
     *
     * @param model
     */
    public void registerForContentChanges(Class<? extends Model> model) {
        if (this.models.contains(model)) {
            return;
        }

        this.models.add(model);
        this.observer.registerForContentChanges(this.getContext(), model);
    }

    @Override
    protected void onStartLoading() {
        if (this.cursor != null) {
            this.deliverResult(this.cursor);
        }

        if (this.takeContentChanged() || this.cursor == null) {
            this.forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Make sure the loading has stopped.
        this.cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        this.stopListeningForChanges();
    }

    @Override
    protected void onReset() {
        super.onReset();

        this.startLoading();

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        cursor = null;

        this.observer.unregisterForContentChanges(this.getContext());
    }

    private void startListeningForChanges() {
        if (!this.listening) {
            this.observer.addModelChangeListener(this.observer);
            this.listening = true;
        }
    }

    private void stopListeningForChanges() {
        if (this.listening) {
            this.observer.removeModelChangeListener(this.observer);
            this.listening = false;
        }
    }

    public Collection<Class<? extends Model>> getModels() {
        return this.models;
    }

    public FlowContentObserver getContentObserver() {
        return this.observer;
    }

    private final class ForceLoadContentObserver extends FlowContentObserver
        implements FlowContentObserver.OnModelStateChangedListener {
        private boolean endOfTransaction = false;

        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onModelStateChanged(@Nullable Class<?> table, BaseModel.Action action, @NonNull SQLCondition[] primaryKeyValues) {
            if (!this.endOfTransaction) {
                if (action == BaseModel.Action.INSERT || action == BaseModel.Action.DELETE || action == BaseModel.Action.UPDATE) {
                    onContentChanged();
                }
            }
        }

        @Override
        public void endTransactionAndNotify() {
            // Mark this as the end of a transactions, and pass control to the base class
            // to perform the notifications.
            this.endOfTransaction = true;
            super.endTransactionAndNotify();

            // Notify the observer the content has changed.
            this.endOfTransaction = false;
            onContentChanged();
        }
    }
}