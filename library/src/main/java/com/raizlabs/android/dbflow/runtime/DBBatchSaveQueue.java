package com.raizlabs.android.dbflow.runtime;

import android.os.Looper;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Description: This queue will bulk save items added to it when it gets access to the DB. It should only exist as one entity.
 * It will save the {@link #MODEL_SAVE_SIZE} at a time or more only when the limit is reached. It will not
 */
public class DBBatchSaveQueue extends Thread {

    /**
     * Once the queue size reaches 50 or larger, the thread will be interrupted and we will batch save the models.
     */
    private static final int MODEL_SAVE_SIZE = 50;

    /**
     * The default time that it will awake the save queue thread to check if any models are still waiting to be saved
     */
    private static final int sMODEL_SAVE_CHECK_TIME = 30000;

    /**
     * Tells how many items to save at a time. This can be set using {@link #setModelSaveSize(int)}
     */
    private int modelSaveSize = MODEL_SAVE_SIZE;

    /**
     * Sets the time we check periodically for leftover {@link com.raizlabs.android.dbflow.structure.Model} in our queue to save.
     */
    private long modelSaveCheckTime = sMODEL_SAVE_CHECK_TIME;

    /**
     * The shared save queue that all {@link com.raizlabs.android.dbflow.runtime.TransactionManager#saveOnSaveQueue(java.util.Collection)} uses.
     */
    private static DBBatchSaveQueue batchSaveQueue;

    /**
     * The list of {@link com.raizlabs.android.dbflow.structure.Model} that we will save here
     */
    private final ArrayList<Model> models;

    /**
     * If true, this queue will quit.
     */
    private boolean isQuitting = false;

    private boolean purgeQueueWhenDone = true;

    private DBTransactionInfo saveQueueInfo = DBTransactionInfo.create("Batch Saving Models");

    private TransactionListener<List<Model>> transactionListener;

    /**
     * Creates a new instance of this class to batch save {@link com.raizlabs.android.dbflow.structure.Model} classes.
     */
    private DBBatchSaveQueue() {
        super("DBBatchSaveQueue");

        models = new ArrayList<>();
    }

    /**
     * Returns the main queue.
     *
     * @return
     */
    public static DBBatchSaveQueue getSharedSaveQueue() {
        if (batchSaveQueue == null) {
            batchSaveQueue = new DBBatchSaveQueue();
        }
        return batchSaveQueue;
    }

    /**
     * Releases the reference to the shared @link com.raizlabs.android.dbflow.runtime.DBBatchSaveQueue}
     */
    public static void disposeSharedQueue() {
        batchSaveQueue = null;
    }

    /**
     * Sets how many models to save at a time in this queue.
     * Increase it for larger batches, but slower recovery time.
     * Smaller the batch, the more time it takes to save overall.
     *
     * @param mModelSaveSize
     */
    public void setModelSaveSize(int mModelSaveSize) {
        this.modelSaveSize = mModelSaveSize;
    }

    /**
     * Change the priority of the queue, add a {@link com.raizlabs.android.dbflow.runtime.transaction.TransactionListener} for when saving is done
     *
     * @param mSaveQueueInfo
     */
    public void setSaveQueueInfo(DBTransactionInfo mSaveQueueInfo) {
        this.saveQueueInfo = mSaveQueueInfo;
    }

    /**
     * Sets a listener to receive call backs as the save queue saves batches of models.
     *
     * @param listener The listener to call as it updates.
     */
    public void setTransactionListener(TransactionListener<List<Model>> listener) {
        transactionListener = listener;
    }

    /**
     * Sets how long, in millis that this queue will check for leftover {@link com.raizlabs.android.dbflow.structure.Model} that have not been saved yet.
     * The default is {@link #sMODEL_SAVE_CHECK_TIME}
     *
     * @param time
     */
    public void setModelSaveCheckTime(long time) {
        this.modelSaveCheckTime = time;
    }

    /**
     * If true, we will awaken the save queue from sleep when the internal {@link TransactionListener} realizes the count of {@link Model}
     * is smaller than the {@link #modelSaveSize}. Default is true.
     *
     * @param purgeQueueWhenDone true to check every batch and if size < {@link #modelSaveSize} in the internal {@link TransactionListener}
     */
    public void setPurgeQueueWhenDone(boolean purgeQueueWhenDone) {
        this.purgeQueueWhenDone = purgeQueueWhenDone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        super.run();
        Looper.prepare();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            final ArrayList<Model> tmpModels;
            synchronized (models) {
                tmpModels = new ArrayList<>(models);
                models.clear();
            }
            if (tmpModels.size() > 0) {
                //onExecute this on the DBManager thread
                TransactionManager.getInstance()
                        .addTransaction(new SaveModelTransaction<>(ProcessModelInfo
                                                                           .withModels(tmpModels)
                                                                           .result(internalListener)
                                                                           .info(saveQueueInfo)));
            }

            try {
                //sleep, and then check for leftovers
                Thread.sleep(modelSaveCheckTime);
            } catch (InterruptedException e) {
                FlowLog.log(FlowLog.Level.I, "DBRequestQueue Batch interrupted to start saving");
            }

            if (isQuitting) {
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
        synchronized (models) {
            models.add(inModel);

            if (models.size() > modelSaveSize) {
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
        synchronized (models) {
            models.addAll(list);

            if (models.size() > modelSaveSize) {
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
        synchronized (models) {
            models.remove(outModel);
        }
    }

    /**
     * Removes a {@link java.util.Collection} of {@link com.raizlabs.android.dbflow.structure.Model} from this queue
     * before it is processed.
     *
     * @param outCollection
     */
    public void removeAll(final Collection<? extends Model> outCollection) {
        synchronized (models) {
            models.removeAll(outCollection);
        }
    }

    /**
     * Quits this queue after it sleeps for the {@link #modelSaveCheckTime}
     */
    public void quit() {
        isQuitting = true;
    }

    private final TransactionListener<List<Model>> internalListener = new TransactionListener<List<Model>>() {
        @Override
        public void onResultReceived(List<Model> result) {
            if (transactionListener != null) {
                transactionListener.onResultReceived(result);
            }

            if (purgeQueueWhenDone) {
                synchronized (models) {
                    // interrupt the thread if we discover the model size is now smaller than the save size.
                    if (models.size() < MODEL_SAVE_SIZE) {
                        purgeQueue();
                    }
                }
            }
        }

        @Override
        public boolean onReady(BaseTransaction<List<Model>> transaction) {
            if (transactionListener != null) {
                // result ignored. Always will be ready.
                transactionListener.onReady(transaction);
            }
            return true;
        }

        @Override
        public boolean hasResult(BaseTransaction<List<Model>> transaction, List<Model> result) {
            if (transactionListener != null) {
                // result ignored always will have a result when called.
                transactionListener.hasResult(transaction, result);
            }
            return true;
        }
    };
}

