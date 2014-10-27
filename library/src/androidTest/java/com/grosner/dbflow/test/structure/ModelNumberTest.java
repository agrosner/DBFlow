package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.structure.Ignore;
import com.grosner.dbflow.test.FlowTestCase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelNumberTest extends FlowTestCase {
    @Override
    protected String getDBName() {
        return "modelnumber";
    }

    @Override
    protected void modifyConfiguration(DBConfiguration.Builder builder) {
        builder.setModelClasses(TestModel1.class);
    }

    // region Test Table Existence

    public void testModelsFound() {
        assertEquals(1, mManager.getStructure().getTableStructure().size());
    }


    @Ignore
    private static class IgnoredModel extends TestModel1 {

    }


    // endregion Test Model Existence
}
