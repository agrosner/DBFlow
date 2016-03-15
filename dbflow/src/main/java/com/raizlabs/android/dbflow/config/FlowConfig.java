package com.raizlabs.android.dbflow.config;

import android.content.Context;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.runtime.BaseTransactionManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Description: The main configuration instance for DBFlow. This
 */
public class FlowConfig {

    private Set<Class<? extends DatabaseHolder>> databaseHolders;
    private BaseTransactionManager transactionManager;
    private Context context;
    private boolean openDatabasesOnInit;

    private FlowConfig(Builder builder) {
        databaseHolders = Collections.unmodifiableSet(builder.databaseHolders);
        transactionManager = builder.transactionManager;
        context = builder.context;
        openDatabasesOnInit = builder.openDatabasesOnInit;
    }

    public Set<Class<? extends DatabaseHolder>> getDatabaseHolders() {
        return databaseHolders;
    }

    public BaseTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    public boolean openDatabasesOnInit() {
        return openDatabasesOnInit;
    }

    public static class Builder {

        final Context context;
        Set<Class<? extends DatabaseHolder>> databaseHolders = new HashSet<>();
        BaseTransactionManager transactionManager;
        boolean openDatabasesOnInit;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder addDatabaseHolder(Class<? extends DatabaseHolder> databaseHolderClass) {
            databaseHolders.add(databaseHolderClass);
            return this;
        }

        public Builder transactionManager(BaseTransactionManager transactionManager) {
            this.transactionManager = transactionManager;
            return this;
        }

        /**
         * @param openDatabasesOnInit true if we want all databases open.
         * @return True to open all associated databases in DBFlow on calling of {@link FlowManager#init(FlowConfig)}
         */
        public Builder openDatabasesOnInit(boolean openDatabasesOnInit) {
            this.openDatabasesOnInit = openDatabasesOnInit;
            return this;
        }

        public FlowConfig build() {
            return new FlowConfig(this);
        }
    }
}
