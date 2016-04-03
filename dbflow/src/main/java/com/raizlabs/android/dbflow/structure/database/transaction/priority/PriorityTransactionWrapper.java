package com.raizlabs.android.dbflow.structure.database.transaction.priority;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;

/**
 * Description: Provides transaction with priority. Meant to be used in a {@link PriorityTransactionQueue}.
 */
public class PriorityTransactionWrapper implements ITransaction, Comparable<PriorityTransactionWrapper> {

    @IntDef({PRIORITY_LOW, PRIORITY_NORMAL, PRIORITY_HIGH, PRIORITY_UI})
    public @interface Priority {
    }

    /**
     * Low priority requests, reserved for non-essential tasks
     */
    public static final int PRIORITY_LOW = 0;

    /**
     * The main of the requests, good for when adding a bunch of
     * data to the DB that the app does not access right away (default).
     */
    public static final int PRIORITY_NORMAL = 1;

    /**
     * Reserved for tasks that will influence user interaction, such as displaying data in the UI
     * some point in the future (not necessarily right away)
     */
    public static final int PRIORITY_HIGH = 2;

    /**
     * Reserved for only immediate tasks and all forms of fetching that will display on the UI
     */
    public static final int PRIORITY_UI = 5;

    private final int priority;
    private final ITransaction transaction;

    PriorityTransactionWrapper(Builder builder) {
        if (builder.priority == 0) {
            priority = PRIORITY_NORMAL;
        } else {
            priority = builder.priority;
        }
        transaction = builder.transaction;
    }

    @Override
    public void execute(DatabaseWrapper databaseWrapper) {
        transaction.execute(databaseWrapper);
    }

    @Override
    public int compareTo(@NonNull PriorityTransactionWrapper another) {
        return another.priority - priority;
    }

    public static class Builder {

        private final ITransaction transaction;
        private int priority;

        public Builder(@NonNull ITransaction transaction) {
            this.transaction = transaction;
        }

        /**
         * Sets a {@link Priority} that orders this transaction.
         */
        public Builder priority(@Priority int priority) {
            this.priority = priority;
            return this;
        }

        public PriorityTransactionWrapper build() {
            return new PriorityTransactionWrapper(this);
        }
    }

}
