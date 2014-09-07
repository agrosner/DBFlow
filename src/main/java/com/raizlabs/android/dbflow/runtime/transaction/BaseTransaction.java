package com.raizlabs.android.dbflow.runtime.transaction;


import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;

/**
 * Created by andrewgrosner
 * Date: 12/11/13
 * Contributors:
 * Description: The basic request object that's placed on the DBRequestQueue for processing.
 * The {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} uses a priority queue that will process
 * this class based on the priority assigned to it.
 *
 * There are four main kinds of requests:
 *  For requests that require UI or immediate retrieval, use PRIORITY_UI
 *  For requests that are displayed in the UI some point in the near future, use PRIORITY_HIGH
 *  For the bulk of data requests, use PRIORITY_NORMAL
 *  For any request that's non-essential use PRIORITY_LOW
 */
public abstract class BaseTransaction<TransactionResult> implements Comparable<BaseTransaction> {

    /**
     * Low priority requests, reserved for non-essential tasks
     */
    public static int PRIORITY_LOW = 0;

    /**
     * The main of the requests, good for when adding a bunch of
     * data to the DB that the app does not access right away.
     */
    public static int PRIORITY_NORMAL = 1;

    /**
     * Reserved for tasks that will influence user interaction, such as displaying data in the UI
     * some point in the future (not necessarily right away)
     */
    public static int PRIORITY_HIGH = 2;

    /**
     * Reserved for only immediate tasks and all forms of fetching that will display on the UI
     */
    public static int PRIORITY_UI = 5;

    /**
     * Tells the queue if this request is ready to run. The default is true. This is run on the
     * {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}'s thread.
     * @return
     */
    public boolean onReady(){
        return true;
    }

    /**
     * Executes this transaction on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}'s thread
     * only if {@link #onReady()} is true.
     */
    public abstract TransactionResult onExecute();

    /**
     * Called When the transaction is completed. This will be run on the UI thread.
     */
    public void onPostExecute(TransactionResult result) {

    }

    private DBTransactionInfo mInfo;

    /**
     * Constructs this class using the specified DBRequest info
     * @param dbTransactionInfo
     */
    public BaseTransaction(DBTransactionInfo dbTransactionInfo) {
        mInfo = dbTransactionInfo;
    }

    /**
     * Creates a new, low priority request
     */
    public BaseTransaction(){
        mInfo = DBTransactionInfo.create();
    }

    @Override
    public int compareTo(BaseTransaction another) {
        return another.mInfo.getPriority() - mInfo.getPriority();
    }

    public String getName() {
        return mInfo.getName();
    }
}
