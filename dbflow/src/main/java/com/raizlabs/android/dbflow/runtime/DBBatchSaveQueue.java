package com.raizlabs.android.dbflow.runtime;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
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
     * Sets the time we check periodically for leftover DB objects in our queue to save.
     */
    private long modelSaveCheckTime = sMODEL_SAVE_CHECK_TIME;

    /**
     * The list of DB objects that we will save here
     */
    private final ArrayList<Object> models;

    /**
     * If true, this queue will quit.
     */
    private boolean isQuitting = false;

    private Transaction.Error errorListener;
    private Transaction.Success successListener;
    private Runnable emptyTransactionListener;

    private DatabaseDefinition databaseDefinition;

    /**
     * Creates a new instance of this class to batch save DB object classes.
     */
    DBBatchSaveQueue(DatabaseDefinition databaseDefinition) {
        super("DBBatchSaveQueue");
        this.databaseDefinition = databaseDefinition;
        models = new ArrayList<>();
    }

    /**
     * Sets how many models to save at a time in this queue.
     * Increase it for larger batches, but slower recovery time.
     * Smaller the batch, the more time it takes to save overall.
     */
    public void setModelSaveSize(int mModelSaveSize) {
        this.modelSaveSize = mModelSaveSize;
    }

    /**
     * Sets how long, in millis that this queue will check for leftover DB objects that have not been saved yet.
     * The default is {@link #sMODEL_SAVE_CHECK_TIME}
     *
     * @param time The time, in millis that queue automatically checks for leftover DB objects in this queue.
     */
    public void setModelSaveCheckTime(long time) {
        this.modelSaveCheckTime = time;
    }


    /**
     * Listener for errors in each batch {@link Transaction}. Called from the DBBatchSaveQueue thread.
     *
     * @param errorListener The listener to use.
     */
    public void setErrorListener(@Nullable Transaction.Error errorListener) {
        this.errorListener = errorListener;
    }

    /**
     * Listener for batch updates. Called from the DBBatchSaveQueue thread.
     *
     * @param successListener The listener to get notified when changes are successful.
     */
    public void setSuccessListener(@Nullable Transaction.Success successListener) {
        this.successListener = successListener;
    }

    /**
     * Listener for when there is no work done. Called from the DBBatchSaveQueue thread.
     *
     * @param emptyTransactionListener The listener to get notified when the save queue thread ran but was empty.
     */
    public void setEmptyTransactionListener(@Nullable Runnable emptyTransactionListener) {
        this.emptyTransactionListener = emptyTransactionListener;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        super.run();
        Looper.prepare();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            final ArrayList<Object> tmpModels;
            synchronized (models) {
                tmpModels = new ArrayList<>(models);
                models.clear();
            }
            if (tmpModels.size() > 0) {
                databaseDefinition.beginTransactionAsync(
                        new ProcessModelTransaction.Builder(modelSaver)
                                .addAll(tmpModels)
                                .build())
                        .success(successCallback)
                        .error(errorCallback)
                        .build()
                        .execute();
            } else if (emptyTransactionListener != null) {
                emptyTransactionListener.run();
            }

            try {
                //sleep, and then check for leftovers
                Thread.sleep(modelSaveCheckTime);
            } catch (InterruptedException e) {
                FlowLog.log(FlowLog.INSTANCE.Level.I, "DBRequestQueue Batch interrupted to start saving");
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
     * Adds an object to this queue.
     */
    public void add(@NonNull final Object inModel) {
        synchronized (models) {
            models.add(inModel);

            if (models.size() > modelSaveSize) {
                interrupt();
            }
        }
    }

    /**
     * Adds a {@link java.util.Collection} of DB objects to this queue
     */
    public void addAll(@NonNull final Collection<Object> list) {
        synchronized (models) {
            models.addAll(list);

            if (models.size() > modelSaveSize) {
                interrupt();
            }
        }
    }

    /**
     * Adds a {@link java.util.Collection} of class that extend Object to this queue
     */
    public void addAll2(@NonNull final Collection<?> list) {
        synchronized (models) {
            models.addAll(list);

            if (models.size() > modelSaveSize) {
                interrupt();
            }
        }
    }

    /**
     * Removes a DB object from this queue before it is processed.
     */
    public void remove(@NonNull final Object outModel) {
        synchronized (models) {
            models.remove(outModel);
        }
    }

    /**
     * Removes a {@link java.util.Collection} of DB object from this queue
     * before it is processed.
     */
    public void removeAll(@NonNull final Collection<Object> outCollection) {
        synchronized (models) {
            models.removeAll(outCollection);
        }
    }

    /**
     * Removes a {@link java.util.Collection} of DB objects from this queue
     * before it is processed.
     */
    public void removeAll2(@NonNull final Collection<?> outCollection) {
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
        public void processModel(Object model, DatabaseWrapper wrapper) {
            if (model instanceof Model) {
                ((Model) model).save();
            } else if (model != null) {
                Class modelClass = model.getClass();
                //noinspection unchecked
                FlowManager.getModelAdapter(modelClass).save(model);
            }
        }
    };

    private final Transaction.Success successCallback = new Transaction.Success() {
        @Override
        public void onSuccess(@NonNull Transaction transaction) {
            if (successListener != null) {
                successListener.onSuccess(transaction);
            }
        }
    };

    private final Transaction.Error errorCallback = new Transaction.Error() {
        @Override
        public void onError(@NonNull Transaction transaction, @NonNull Throwable error) {
            if (errorListener != null) {
                errorListener.onError(transaction, error);
            }
        }
    };

}

