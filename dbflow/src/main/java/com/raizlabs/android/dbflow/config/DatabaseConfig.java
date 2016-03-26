package com.raizlabs.android.dbflow.config;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 */
public final class DatabaseConfig {

    public interface HelperCreator {

        OpenHelper createHelper(DatabaseDefinition databaseDefinition, DatabaseHelperListener helperListener);
    }

    private final HelperCreator helperCreator;
    private final Class<?> databaseClass;
    private final BaseTransactionManager transactionManager;
    private final DatabaseHelperListener helperListener;
    private final Map<Class<? extends Model>, TableConfig> tableConfigMap;


    DatabaseConfig(Builder builder) {
        helperCreator = builder.helperCreator;
        databaseClass = builder.databaseClass;
        transactionManager = builder.transactionManager;
        helperListener = builder.helperListener;
        tableConfigMap = builder.tableConfigMap;
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

    public Map<Class<? extends Model>, TableConfig> tableConfigMap() {
        return tableConfigMap;
    }

    public static final class Builder {

        HelperCreator helperCreator;
        final Class<?> databaseClass;
        BaseTransactionManager transactionManager;
        DatabaseHelperListener helperListener;
        final Map<Class<? extends Model>, TableConfig> tableConfigMap = new HashMap<>();


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

        public Builder addTableConfig(TableConfig<?> tableConfig) {
            tableConfigMap.put(tableConfig.tableClass(), tableConfig);
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
