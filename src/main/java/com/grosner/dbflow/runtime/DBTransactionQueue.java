package com.grosner.dbflow.runtime;

import android.os.Looper;

import com.grosner.dbflow.runtime.transaction.BaseTransaction;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by andrewgrosner
 * Date: 12/11/13
 * Contributors:
 * Description: will handle concurrent requests to the DB based on priority
 */
public class DBTransactionQueue extends Thread{

    /**
     * Queue of requests
     */
    private final PriorityBlockingQueue<BaseTransaction> mQueue;

    private boolean mQuit = false;

    /**
     * Creates a queue with the specified name to ID it.
     * @param name
     */
    public DBTransactionQueue(String name) {
        super(name);

        mQueue = new PriorityBlockingQueue<BaseTransaction>();
    }

    @Override
    public void run() {
        Looper.prepare();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        BaseTransaction transaction;
        while (true){
            try{
                transaction = mQueue.take();
            } catch (InterruptedException e){
                if(mQuit){
                    synchronized (mQueue) {
                        mQueue.clear();
                    }
                    return;
                }
                continue;
            }

            try{
                // If the transaction is ready
                if(transaction.onReady()) {
                    //if(AALog.isEnabled()) {
                    //AALog.d("DBRequestQueue + " + getName(), "Size is: " + mQueue.size() + " executing:" + runnable.getName());
                    //}
                    // Retrieve the result of the transaction
                    final Object result = transaction.onExecute();
                    final BaseTransaction finalTransaction = transaction;

                    // Run the result on the FG
                    DatabaseManager.getInstance().processOnRequestHandler(new Runnable() {
                        @Override
                        public void run() {
                            finalTransaction.onPostExecute(result);
                        }
                    });
                }
            } catch (Throwable t){
                throw new RuntimeException(t);
            }
        }

    }

    public void add(BaseTransaction runnable){
        if (!mQueue.contains(runnable)) {
            mQueue.add(runnable);
        }
    }

    /**
     * Cancels the specified request.
     * @param runnable
     */
    public void cancel(BaseTransaction runnable){
        if (mQueue.contains(runnable)) {
            mQueue.remove(runnable);
        }
    }

    /**
     * Cancels all requests by a specific tag
     * @param tag
     */
    public void cancel(String tag){
        synchronized (mQueue){
            Iterator<BaseTransaction> it = mQueue.iterator();
            while(it.hasNext()){
                BaseTransaction next = it.next();
                if(next.getName().equals(tag)){
                    it.remove();
                }
            }
        }
    }

    /**
     * Quits this process
     */
    public void quit(){
        mQuit = true;
        interrupt();
    }
}

