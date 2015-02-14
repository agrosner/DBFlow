package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.test.FlowTestCase;

/**
 * Description:
 */
public class DefaultModelTest extends FlowTestCase {

    public void testDefaultModel() {
        Delete.table(DefaultModel.class);
        DefaultModel defaultModel = new DefaultModel();
        defaultModel.name = "Test";
        defaultModel.save(false);
    }
}
