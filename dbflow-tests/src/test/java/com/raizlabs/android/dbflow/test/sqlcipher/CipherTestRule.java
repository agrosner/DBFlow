package com.raizlabs.android.dbflow.test.sqlcipher;

import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.robolectric.RuntimeEnvironment;

/**
 * Description:
 */
public class CipherTestRule implements TestRule {

    public static CipherTestRule create() {
        return new CipherTestRule();
    }

    private CipherTestRule() {
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                FlowManager.init(new FlowConfig.Builder(RuntimeEnvironment.application)
                        .addDatabaseConfig(new DatabaseConfig.Builder(CipherDatabase.class)
                                .openHelper(new DatabaseConfig.OpenHelperCreator() {
                                    @Override
                                    public OpenHelper createHelper(DatabaseDefinition databaseDefinition, DatabaseHelperListener helperListener) {
                                        return new SQLCipherHelperImpl(databaseDefinition, helperListener);
                                    }
                                }).build()).build());
                try {
                    base.evaluate();
                } finally {
                    FlowManager.getDatabase(CipherDatabase.class).reset(RuntimeEnvironment.application);
                    FlowManager.destroy();
                }
            }
        };
    }
}
