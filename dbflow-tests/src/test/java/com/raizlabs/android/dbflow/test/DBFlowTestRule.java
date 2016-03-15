package com.raizlabs.android.dbflow.test;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.test.sql.MigrationDatabase;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.robolectric.RuntimeEnvironment;

/**
 * Description: The main rule for tests.
 */
public class DBFlowTestRule implements TestRule {

    public static DBFlowTestRule create() {
        return new DBFlowTestRule();
    }

    private DBFlowTestRule() {
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                FlowManager.init(new FlowConfig.Builder(RuntimeEnvironment.application).build());
                try {
                    base.evaluate();
                } finally {
                    FlowManager.getDatabase(TestDatabase.NAME).reset(RuntimeEnvironment.application);
                    FlowManager.getDatabase(MigrationDatabase.NAME).reset(RuntimeEnvironment.application);
                    FlowManager.destroy();
                }
            }
        };
    }
}
