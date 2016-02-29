package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.global.GlobalModel;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class FixPrimaryKeyMigrationTest extends FlowTestCase {

    FixPrimaryKeyMigration<GlobalModel> fixPrimaryKeyMigration;
    ModelAdapter modelAdapter;

    @Before
    public void setUpMigration() {
        fixPrimaryKeyMigration = new FixPrimaryKeyMigration<GlobalModel>() {
            @Override
            protected Class<GlobalModel> getTableClass() {
                return GlobalModel.class;
            }
        };
        modelAdapter = FlowManager.getModelAdapter(fixPrimaryKeyMigration.getTableClass());
    }


    @Test
    public void test_validateTableInformationQuery() {
        assertEquals("SELECT sql FROM sqlite_master WHERE name='GlobalModel'", fixPrimaryKeyMigration.getSelectTableQuery().getQuery());
        assertEquals("CREATE TABLE IF NOT EXISTS `GlobalModel`(`id` INTEGER,`name` TEXT, PRIMARY KEY(`id`)" + ");", modelAdapter.getCreationQuery());
        assertTrue(fixPrimaryKeyMigration.validateCreationQuery(modelAdapter.getCreationQuery()));
    }

    @Test
    public void test_validateTempCreationQuery() {
        assertEquals("CREATE TABLE IF NOT EXISTS `GlobalModel_temp`(`id` INTEGER,`name` TEXT, PRIMARY KEY(`id`)" + ");", fixPrimaryKeyMigration.getTempCreationQuery());
    }
}
