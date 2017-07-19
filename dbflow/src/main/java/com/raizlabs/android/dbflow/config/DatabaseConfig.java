package com.raizlabs.android.dbflow.config;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.StringUtils;
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager;
import com.raizlabs.android.dbflow.runtime.ModelNotifier;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 */
public final class DatabaseConfig {

    public static DatabaseConfig.Builder builder(@NonNull Class<?> database) {
        return new DatabaseConfig.Builder(database);
    }

    public static DatabaseConfig.Builder inMemoryBuilder(@NonNull Class<?> database) {
        return new DatabaseConfig.Builder(database).inMemory();
    }

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
    private final ModelNotifier modelNotifier;
    private final boolean inMemory;
    private final String databaseName;
    private final String databaseExtensionName;

    DatabaseConfig(Builder builder) {
        openHelperCreator = builder.openHelperCreator;
        databaseClass = builder.databaseClass;
        transactionManagerCreator = builder.transactionManagerCreator;
        helperListener = builder.helperListener;
        tableConfigMap = builder.tableConfigMap;
        modelNotifier = builder.modelNotifier;
        inMemory = builder.inMemory;
        if (builder.databaseName == null) {
            databaseName = builder.databaseClass.getSimpleName();
        } else {
            databaseName = builder.databaseName;
        }

        if (builder.databaseExtensionName == null) {
            databaseExtensionName = ".db";
        } else {
            databaseExtensionName = StringUtils.isNotNullOrEmpty(builder.databaseExtensionName)
                ? "." + builder.databaseExtensionName : "";
        }
    }

    @NonNull
    public String getDatabaseExtensionName() {
        return databaseExtensionName;
    }

    public boolean isInMemory() {
        return inMemory;
    }

    @NonNull
    public String getDatabaseName() {
        return databaseName;
    }

    @Nullable
    public OpenHelperCreator helperCreator() {
        return openHelperCreator;
    }

    @Nullable
    public DatabaseHelperListener helperListener() {
        return helperListener;
    }

    @NonNull
    public Class<?> databaseClass() {
        return databaseClass;
    }

    @Nullable
    public TransactionManagerCreator transactionManagerCreator() {
        return transactionManagerCreator;
    }

    @Nullable
    public ModelNotifier modelNotifier() {
        return modelNotifier;
    }

    @NonNull
    public Map<Class<?>, TableConfig> tableConfigMap() {
        return tableConfigMap;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <TModel> TableConfig<TModel> getTableConfigForTable(Class<TModel> modelClass) {
        return tableConfigMap().get(modelClass);
    }

    public static final class Builder {

        OpenHelperCreator openHelperCreator;
        final Class<?> databaseClass;
        TransactionManagerCreator transactionManagerCreator;
        DatabaseHelperListener helperListener;
        final Map<Class<?>, TableConfig> tableConfigMap = new HashMap<>();
        ModelNotifier modelNotifier;
        boolean inMemory = false;
        String databaseName;
        String databaseExtensionName;

        public Builder(@NonNull Class<?> databaseClass) {
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

        public Builder modelNotifier(ModelNotifier modelNotifier) {
            this.modelNotifier = modelNotifier;
            return this;
        }

        @NonNull
        public Builder inMemory() {
            inMemory = true;
            return this;
        }

        /**
         * @return Pass in dynamic database name here. Otherwise it defaults to class name.
         */
        @NonNull
        public Builder databaseName(String name) {
            databaseName = name;
            return this;
        }

        /**
         * @return Pass in the extension for the DB here.
         * Otherwise defaults to ".db". If empty string passed, no extension is used.
         */
        public Builder extensionName(String name) {
            databaseExtensionName = name;
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
