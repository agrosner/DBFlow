package com.raizlabs.android.dbflow.config;

import com.raizlabs.android.dbflow.runtime.BaseTransactionManager;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 */
public final class DatabaseConfig {

    public interface OpenHelperCreator {

        OpenHelper createHelper(DatabaseDefinition databaseDefinition, DatabaseHelperListener helperListener);
    }

    public interface TransactionManagerCreator {

        BaseTransactionManager createManager(DatabaseDefinition databaseDefinition);
    }

    private final OpenHelperCreator openHelperCreator;
    private final Class<?> databaseClass;
    private final TransactionManagerCreator transactionManagerCreator;
    private final DatabaseHelperListener helperListener;
    private final Map<Class<?>, TableConfig> tableConfigMap;


    DatabaseConfig(Builder builder) {
        openHelperCreator = builder.openHelperCreator;
        databaseClass = builder.databaseClass;
        transactionManagerCreator = builder.transactionManagerCreator;
        helperListener = builder.helperListener;
        tableConfigMap = builder.tableConfigMap;
    }

    public OpenHelperCreator helperCreator() {
        return openHelperCreator;
    }

    public DatabaseHelperListener helperListener() {
        return helperListener;
    }

    public Class<?> databaseClass() {
        return databaseClass;
    }

    public TransactionManagerCreator transactionManagerCreator() {
        return transactionManagerCreator;
    }

    public Map<Class<?>, TableConfig> tableConfigMap() {
        return tableConfigMap;
    }

    @SuppressWarnings("unchecked")
    public <TModel> TableConfig<TModel> getTableConfigForTable(Class<TModel> modelClass) {
        return tableConfigMap().get(modelClass);
    }

    public static final class Builder {

        OpenHelperCreator openHelperCreator;
        final Class<?> databaseClass;
        TransactionManagerCreator transactionManagerCreator;
        DatabaseHelperListener helperListener;
        final Map<Class<?>, TableConfig> tableConfigMap = new HashMap<>();


        public Builder(Class<?> databaseClass) {
            this.databaseClass = databaseClass;
        }

        public Builder transactionManagerCreator(TransactionManagerCreator transactionManager) {
            this.transactionManagerCreator = transactionManager;
            return this;
        }

        public Builder helperListener(DatabaseHelperListener helperListener) {
            this.helperListener = helperListener;
            return this;
        }

        public Builder addTableConfig(TableConfig<?> tableConfig) {
            tableConfigMap.put(tableConfig.tableClass(), tableConfig);
            return this;
        }

        /**
         * Overrides the default {@link OpenHelper} for a {@link DatabaseDefinition}.
         *
         * @param openHelper The openhelper to use.
         */
        public Builder openHelper(OpenHelperCreator openHelper) {
            openHelperCreator = openHelper;
            return this;
        }

        public DatabaseConfig build() {
            return new DatabaseConfig(this);
        }
    }
}
