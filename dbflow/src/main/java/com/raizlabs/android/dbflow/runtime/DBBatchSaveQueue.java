package com.raizlabs.android.dbflow.runtime;

import android.os.Looper;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionManager;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.ArrayList;
import java.util.Collection;

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
     * The shared save queue that all {@link DefaultTransactionManager#saveOnSaveQueue(java.util.Collection)} uses.
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

    private Transaction.Error errorListener;
    private Transaction.Success successListener;

    private DatabaseDefinition databaseDefinition;

    /**
     * Creates a new instance of this class to batch save {@link com.raizlabs.android.dbflow.structure.Model} classes.
     */
    DBBatchSaveQueue(DatabaseDefinition databaseDefinition) {
        super("DBBatchSaveQueue");
        this.databaseDefinition = databaseDefinition;
        models = new ArrayList<>();
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
     * Sets how long, in millis that this queue will check for leftover {@link com.raizlabs.android.dbflow.structure.Model} that have not been saved yet.
     * The default is {@link #sMODEL_SAVE_CHECK_TIME}
     *
     * @param time The time, in millis that queue automatically checks for leftover {@link Model} in this queue.
     */
    public void setModelSaveCheckTime(long time) {
        this.modelSaveCheckTime = time;
    }

    /**
     * If true, we will awaken the save queue from sleep when the internal {@link TransactionListener} realizes the count of {@link Model}
     * is smaller than the {@link #modelSaveSize}. Default is true.
     *
     * @param purgeQueueWhenDone true to check every batch and if size &lt; {@link #modelSaveSize} in the internal {@link TransactionListener}
     */
    public void setPurgeQueueWhenDone(boolean purgeQueueWhenDone) {
        this.purgeQueueWhenDone = purgeQueueWhenDone;
    }

    /**
     * Listener for errors in each batch {@link Transaction}.
     *
     * @param errorListener The listener to use.
     */
    public void setErrorListener(Transaction.Error errorListener) {
        this.errorListener = errorListener;
    }

    /**
     * Listener for batch updates.
     *
     * @param successListener The listener to get notified when changes are successful.
     */
    public void setSuccessListener(Transaction.Success successListener) {
        this.successListener = successListener;
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

                databaseDefinition.beginTransactionAsync(
                    new ProcessModelTransaction.Builder(modelSaver).build())
                    .success(successCallback)
                    .error(errorCallback)
                    .build()
                    .execute();
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
     * @param <TModel>
     */
    public <TModel extends Model> void addAll(final Collection<TModel> list) {
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

    private final ProcessModelTransaction.ProcessModel modelSaver = new ProcessModelTransaction.ProcessModel() {
        @Override
        public void processModel(Model model) {
            model.save();
        }
    };

    private final Transaction.Success successCallback = new Transaction.Success() {
        @Override
        public void onSuccess(Transaction transaction) {
            if (successListener != null) {
                successListener.onSuccess(transaction);
            }
        }
    };

    private final Transaction.Error errorCallback = new Transaction.Error() {
        @Override
        public void onError(Transaction transaction, Throwable error) {
            if (errorListener != null) {
                errorListener.onError(transaction, error);
            }
        }
    };

}

