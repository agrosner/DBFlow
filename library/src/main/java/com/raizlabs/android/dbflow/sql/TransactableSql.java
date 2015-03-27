package com.raizlabs.android.dbflow.sql;

import com.raizlabs.android.dbflow.runtime.FlowContentObserver;

/**
 * Description: Classes implementing this method can batch modification operations into a Transaction and
 * notify a {@link FlowContentObserver} about it's changes after.
 */
public interface TransactableSql {

    void beginTransaction();

    void endTransactionAndNotify();
}
