package com.raizlabs.android.dbflow.runtime;

import android.os.Looper;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by andrewgrosner
 * Date: 3/19/14
 * Contributors:
 * Description: This queue will bulk save items added to it when it gets access to the DB. It should only exist as one entity.
 * It will save the {@link #sMODEL_SAVE_SIZE} at a time or more only when the limit is reached. It will not
 */
public class DBBatchSaveQueue extends Thread {

    /**
     * Once the queue size reaches 50 or larger, the thread will be interrupted and we will batch save the models.
     */
    private static final int sMODEL_SAVE_SIZE = 50;
    /**
     * Tells how many items to save at a time. This can be set using {@link #setModelSaveSize(int)}
     */
    private int mModelSaveSize = sMODEL_SAVE_SIZE;
    /**
     * The default time that it will awake the save queue thread to check if any models are still waiting to be saved
     */
    private static final int sMODEL_SAVE_CHECK_TIME = 30000;
    /**
     * Sets the time we check periodically for leftover {@link com.raizlabs.android.dbflow.structure.Model} in our queue to save.
     */
    private long mModelSaveCheckTime = sMODEL_SAVE_CHECK_TIME;
    /**
     * The shared save queue that all {@link com.raizlabs.android.dbflow.runtime.TransactionManager#saveOnSaveQueue(java.util.Collection)} uses.
     */
    private static DBBatchSaveQueue mBatchSaveQueue;
    /**
     * The list of {@link com.raizlabs.android.dbflow.structure.Model} that we will save here
     */
    private final ArrayList<Model> mModels;
    /**
     * If true, this queue will quit.
     */
    private boolean mQuit = false;

    private DBTransactionInfo mSaveQueueInfo = DBTransactionInfo.create("Batch Saving Models");

    /**
     * Creates a new instance of this class to batch save {@link com.raizlabs.android.dbflow.structure.Model} classes.
     */
    private DBBatchSaveQueue() {
        super("DBBatchSaveQueue");

        mModels = new ArrayList<Model>();
    }

    /**
     * Returns the main queue.
     *
     * @return
     */
    public static DBBatchSaveQueue getSharedSaveQueue() {
        if (mBatchSaveQueue == null) {
            mBatchSaveQueue = new DBBatchSaveQueue();
        }
        return mBatchSaveQueue;
    }

    /**
     * Releases the reference to the shared @link com.raizlabs.android.dbflow.runtime.DBBatchSaveQueue}
     */
    public static void disposeSharedQueue() {
        mBatchSaveQueue = null;
    }

    /**
     * Sets how many models to save at a time in this queue.
     * Increase it for larger batches, but slower recovery time.
     * Smaller the batch, the more time it takes to save overall.
     *
     * @param mModelSaveSize
     */
    public void setModelSaveSize(int mModelSaveSize) {
        this.mModelSaveSize = mModelSaveSize;
    }

    /**
     * Change the priority of the queue, add a {@link com.raizlabs.android.dbflow.runtime.transaction.TransactionListener} for when saving is done
     * @param mSaveQueueInfo
     */
    public void setSaveQueueInfo(DBTransactionInfo mSaveQueueInfo) {
        this.mSaveQueueInfo = mSaveQueueInfo;
    }

    /**
     * Sets how long, in millis that this queue will check for leftover {@link com.raizlabs.android.dbflow.structure.Model} that have not been saved yet.
     * The default is {@link #sMODEL_SAVE_CHECK_TIME}
     *
     * @param time
     */
    public void setModelSaveCheckTime(long time) {
        this.mModelSaveCheckTime = time;
    }

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            final ArrayList<Model> tmpModels;
            synchronized (mModels) {
                tmpModels = new ArrayList<Model>(mModels);
                mModels.clear();
            }
            if (tmpModels.size() > 0) {
                //onExecute this on the DBManager thread
                TransactionManager.getInstance()
                        .addTransaction(new SaveModelTransaction<>(ProcessModelInfo
                                                .withModels(tmpModels)
                                                .info(mSaveQueueInfo)));
            }

            try {
                //sleep, and then check for leftovers
                Thread.sleep(mModelSaveCheckTime);
            } catch (InterruptedException e) {
                FlowLog.log(FlowLog.Level.I, "DBRequestQueue Batch interrupted to start saving");
            }

            if (mQuit) {
                return;
            }
        }
    }

    /**
     * Will cause the queue to wake from sleep and handle it's current list of items.
     */
    public void purgeQueue() {
        interrupt();
    }

    /**
     * Adds a {@link com.raizlabs.android.dbflow.structure.Model} to this queue.
     *
     * @param inModel
     */
    public void add(final Model inModel) {
        synchronized (mModels) {
            mModels.add(inModel);

            if (mModels.size() > mModelSaveSize) {
                interrupt();
            }
        }
    }

    /**
     * Adds a {@link java.util.Collection} of {@link com.raizlabs.android.dbflow.structure.Model} to this queue
     *
     * @param list
     * @param <ModelClass>
     */
    public <ModelClass extends Model> void addAll(final Collection<ModelClass> list) {
        synchronized (mModels) {
            mModels.addAll(list);

            if (mModels.size() > mModelSaveSize) {
                interrupt();
            }
        }
    }

    /**
     * Removes a {@link com.raizlabs.android.dbflow.structure.Model} from this queue before it is processed.
     *
     * @param outModel
     */
    public void remove(final Model outModel) {
        synchronized (mModels) {
            mModels.remove(outModel);
        }
    }

    /**
     * Removes a {@link java.util.Collection} of {@link com.raizlabs.android.dbflow.structure.Model} from this queue
     * before it is processed.
     *
     * @param outCollection
     */
    public void removeAll(final Collection<? extends Model> outCollection) {
        synchronized (mModels) {
            mModels.removeAll(outCollection);
        }
    }

    /**
     * Quits this queue after it sleeps for the {@link #mModelSaveCheckTime}
     */
    public void quit() {
        mQuit = true;
    }
}

