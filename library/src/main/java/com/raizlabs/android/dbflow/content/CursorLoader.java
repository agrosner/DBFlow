package com.raizlabs.android.dbflow.content;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.ModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Static library support version of the framework's {@link android.content.CursorLoader}. Used to
 * write apps that run on platforms prior to Android 3.0.  When running on Android 3.0 or above,
 * this implementation is still used; it does not try to switch to the framework's implementation.
 * See the framework SDK documentation for a class overview.
 */
public class CursorLoader extends AsyncTaskLoader<Cursor> {
    final ForceLoadContentObserver mObserver;

    ModelQueriable<? extends Model> mModelQueriable;
    Cursor mCursor;

    private final class ForceLoadContentObserver extends FlowContentObserver {
        public ForceLoadContentObserver() {
            // super(new Handler());
        }

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

    /* Runs on a worker thread */
    @Override
    public Cursor loadInBackground() {
        Cursor cursor = mModelQueriable.query();
        if (cursor != null) {
            // Ensure the cursor window is filled
            cursor.getCount();
        }
        return cursor;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Creates an empty unspecified CursorLoader.  You must follow this with calls to {@link
     * #setModelQueriable(ModelQueriable<? extends Model>)}}, etc to specify the query to perform.
     */
    public CursorLoader(Context context) {
        super(context);
        mObserver = new ForceLoadContentObserver();
    }

    /**
     * Creates a fully-specified CursorLoader.  See {@link android.content.ContentResolver#query(Uri,
     * String[], String, String[], String) ContentResolver.query()} for documentation on the meaning
     * of the parameters.  These will be passed as-is to that call.
     */
    public CursorLoader(Context context, ModelQueriable<? extends Model> modelQueriable) {
        super(context);
        mModelQueriable = modelQueriable;
        mObserver = new ForceLoadContentObserver();
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
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }

        mObserver.registerForContentChanges(getContext(), mModelQueriable.getTable());
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        // mObserver.unregisterForContentChanges(getContext());
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;

        mObserver.unregisterForContentChanges(getContext());
    }

    public void setModelQueriable(ModelQueriable<? extends Model> modelQueriable) {
        this.mModelQueriable = modelQueriable;
    }

    public ModelQueriable<? extends Model> getModelQueriable() {
        return mModelQueriable;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix);
        writer.print(prefix);
        writer.print("mModelQueriable=");
        writer.print(mModelQueriable);
        writer.print(prefix);
        writer.print("mCursor=");
        writer.println(mCursor);
        writer.print(prefix); writer.print("mContentChanged="); writer.println(takeContentChanged());
    }
}
