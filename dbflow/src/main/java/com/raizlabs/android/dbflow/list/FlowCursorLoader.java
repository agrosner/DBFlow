package com.raizlabs.android.dbflow.list;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import android.support.v4.content.AsyncTaskLoader;

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
@TargetApi (11)
public class FlowCursorLoader extends AsyncTaskLoader<Cursor>
{
  private static final String TAG = "FlowCursorLoader";

  /// Models to be observed for changes.
  private HashSet<Class<? extends Model>> mModels = new HashSet<> ();

  /// Queriable operation that the loader executes.
  private Queriable mQueriable;

  /// Cursor for the loader.
  private Cursor mCursor;

  private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver ();

  private boolean mListening = false;

  /**
   * Creates a fully-specified CursorLoader.  See {@link android.content.ContentResolver#query(Uri,
   * String[], String, String[], String) ContentResolver.query()} for documentation on the meaning
   * of the parameters.  These will be passed as-is to that call.
   */
  public FlowCursorLoader (Context context, Queriable queriable)
  {
    super (context);

    this.mQueriable = queriable;
  }

  @Override
  public Cursor loadInBackground ()
  {
    Cursor cursor = this.mQueriable.query ();

    if (cursor != null)
      cursor.getCount ();

    return cursor;
  }

  @Override
  public void deliverResult (Cursor cursor)
  {
    if (this.isReset ())
    {
      // An async query came in while the loader is stopped
      if (cursor != null)
        cursor.close ();

      return;
    }

    Cursor oldCursor = this.mCursor;
    this.mCursor = cursor;

    if (this.isStarted ())
      super.deliverResult (cursor);

    if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed ())
      oldCursor.close ();

    // Now that the result has been delivered, start listening for changes
    // to the target models. Doing this at anytime earlier runs the risk of
    // listening for changes while we are still loading content.
    this.startListeningForChanges ();
  }

  /**
   * Register the loader for changes to a Flow model. When changes to the model are
   * detected, then the loader will automatically reload the content.
   *
   * @param model
   */
  public void registerForContentChanges (Class<? extends Model> model)
  {
    if (this.mModels.contains (model))
      return;

    this.mModels.add (model);
    this.mObserver.registerForContentChanges (this.getContext (), model);
  }

  @Override
  protected void onStartLoading ()
  {
    if (this.mCursor != null)
      this.deliverResult (this.mCursor);

    if (this.takeContentChanged () || this.mCursor == null)
      this.forceLoad ();
  }

  @Override
  protected void onStopLoading ()
  {
    // Make sure the loading has stopped.
    this.cancelLoad ();
  }

  @Override
  public void onCanceled (Cursor cursor)
  {
    if (cursor != null && !cursor.isClosed ())
      cursor.close ();

    this.stopListeningForChanges ();
  }

  @Override
  protected void onReset ()
  {
    // Ensure the loader is stopped
    this.onStopLoading ();

    if (mCursor != null && !mCursor.isClosed ())
      mCursor.close ();

    mCursor = null;

    this.mObserver.unregisterForContentChanges (this.getContext ());
  }

  private void startListeningForChanges ()
  {
    if (!this.mListening)
    {
      this.mObserver.addModelChangeListener (this.mObserver);
      this.mListening = true;
    }
  }

  private void stopListeningForChanges ()
  {
    if (this.mListening)
    {
      this.mObserver.removeModelChangeListener (this.mObserver);
      this.mListening = false;
    }
  }

  public Collection<Class<? extends Model>> getModels ()
  {
    return this.mModels;
  }

  public FlowContentObserver getContentObserver ()
  {
    return this.mObserver;
  }

  final class ForceLoadContentObserver extends FlowContentObserver
      implements FlowContentObserver.OnModelStateChangedListener
  {
    private boolean endOfTransaction = false;

    @Override
    public boolean deliverSelfNotifications ()
    {
      return false;
    }

    @Override
    public void onModelStateChanged (Class<? extends Model> table,
                                     BaseModel.Action action,
                                     SQLCondition[] primaryKeyValues)
    {
      if (!this.endOfTransaction)
      {
        if (action == BaseModel.Action.INSERT ||
            action == BaseModel.Action.DELETE ||
            action == BaseModel.Action.UPDATE)
        {
          onContentChanged ();
        }
      }
    }

    @Override
    public void endTransactionAndNotify ()
    {
      // Mark this as the end of a transactions, and pass control to the base class
      // to perform the notifications.
      this.endOfTransaction = true;
      super.endTransactionAndNotify ();

      // Notify the observer the content has changed.
      this.endOfTransaction = false;
      onContentChanged ();
    }
  }
}