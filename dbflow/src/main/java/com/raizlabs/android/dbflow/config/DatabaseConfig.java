package com.raizlabs.android.dbflow.config;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;

/**
 * Description:
 */
public class DatabaseConfig {

    public interface HelperCreator {

        OpenHelper createHelper(DatabaseDefinition databaseDefinition, DatabaseHelperListener helperListener);
    }

    private final HelperCreator helperCreator;
    private final Class<?> databaseClass;
    private final BaseTransactionManager transactionManager;
    private final DatabaseHelperListener helperListener;


    DatabaseConfig(Builder builder) {
        helperCreator = builder.helperCreator;
        databaseClass = builder.databaseClass;
        transactionManager = builder.transactionManager;
        helperListener = builder.helperListener;
    }

    public HelperCreator helperCreator() {
        return helperCreator;
    }

    public DatabaseHelperListener helperListener() {
        return helperListener;
    }

    public Class<?> databaseClass() {
        return databaseClass;
    }

    public BaseTransactionManager transactionManager() {
        return transactionManager;
    }

    public static class Builder {

        HelperCreator helperCreator;
        final Class<?> databaseClass;
        BaseTransactionManager transactionManager;
        DatabaseHelperListener helperListener;


        public Builder(Class<?> databaseClass) {
            this.databaseClass = databaseClass;
        }

        public Builder transactionManager(BaseTransactionManager transactionManager) {
            this.transactionManager = transactionManager;
            return this;
        }

        public Builder helperListener(DatabaseHelperListener helperListener) {
            this.helperListener = helperListener;
            return this;
        }

        /**
         * Overrides the default {@link OpenHelper} for a {@link DatabaseDefinition}.
         *
         * @param openHelper The openhelper to use.
         */
        public Builder openHelper(HelperCreator openHelper) {
            helperCreator = openHelper;
            return this;
        }

        public DatabaseConfig build() {
            return new DatabaseConfig(this);
        }
    }
}
