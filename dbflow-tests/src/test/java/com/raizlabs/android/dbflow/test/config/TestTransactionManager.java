package com.raizlabs.android.dbflow.test.config;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

/**
 * Description: Used for testing integration.
 */
public class TestTransactionManager extends BaseTransactionManager {

    public TestTransactionManager(@NonNull DatabaseDefinition databaseDefinition) {
        super(new CustomQueue(), databaseDefinition);
    }

    public static class CustomQueue implements ITransactionQueue {

        @Override
        public void add(Transaction transaction) {

        }

        @Override
        public void cancel(Transaction transaction) {

        }

        @Override
        public void startIfNotAlive() {

        }

        @Override
        public void cancel(String name) {

        }

        @Override
        public void quit() {

        }
    }
}
