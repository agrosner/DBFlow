package com.raizlabs.android.dbflow.runtime;

import android.os.Looper;

import java.util.ArrayList;
import java.util.Collection;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Created by andrewgrosner
 * Date: 3/19/14
 * Contributors:
 * Description: This queue will bulk save items added to it when it gets access to the DB. It should only exist as one entity.
 */
public class DBBatchSaveQueue extends Thread{

    private static DBBatchSaveQueue mBatchSaveQueue;

    /**
     *  Once the queue size reaches 50 or larger, the thread will be interrupted and we will batch save the models.
     */
    private static final int sMODEL_SAVE_SIZE = 50;

    /**
     * Tells how many items to save at a time. This can be set using {@link #setModelSaveSize(int)}
     */
    private int mModelSaveSize = sMODEL_SAVE_SIZE;

    private boolean mQuit = false;

    public static DBBatchSaveQueue getSharedSaveQueue(){
        if(mBatchSaveQueue==null){
            mBatchSaveQueue = new DBBatchSaveQueue();
        }
        return mBatchSaveQueue;
    }

    public static void disposeSharedQueue(){
        mBatchSaveQueue = null;
    }

    private final ArrayList<Model> mModels;

    public DBBatchSaveQueue(){
        super("DBBatchSaveQueue");

        mModels = new ArrayList<Model>();
    }

    /**
     * Sets how many models to save at a time in this queue.
     * Increase it for larger batches, but slower recovery time.
     * Smaller the batch, the more time it takes to save overall.
     * @param mModelSaveSize
     */
    public void setModelSaveSize(int mModelSaveSize) {
        this.mModelSaveSize = mModelSaveSize;
    }

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        while (true){
            final ArrayList<Model> tmpModels;
            synchronized (mModels){
                tmpModels = new ArrayList<Model>(mModels);
                mModels.clear();
            }
            if(tmpModels.size()>0) {
                //onExecute this on the DBManager thread
                DatabaseManager.getSharedInstance().save(DBTransactionInfo.create("Batch Saving"), null, tmpModels);
            }

            try {
                //sleep for 5 mins, and then check for leftovers
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                //AALog.d("DBBatchSaveQueue", "Batch interrupted to start saving");
            }

            if(mQuit){
                return;
            }
        }
    }

    public void add(final Model inModel){
        synchronized (mModels){
            mModels.add(inModel);

            if(mModels.size()>mModelSaveSize){
                interrupt();
            }
        }
    }

    public <ModelClass extends Model> void addAll(final Collection<ModelClass> list){
        synchronized (mModels){
            mModels.addAll(list);

            if(mModels.size()>mModelSaveSize){
                interrupt();
            }
        }
    }

    public void remove(final Model outModel){
        synchronized (mModels){
            mModels.remove(outModel);
        }
    }

    public void removeAll(final Collection outCollection){
        synchronized (mModels){
            mModels.removeAll(outCollection);
        }
    }

    public void quit() {
        mQuit = true;
    }
}

