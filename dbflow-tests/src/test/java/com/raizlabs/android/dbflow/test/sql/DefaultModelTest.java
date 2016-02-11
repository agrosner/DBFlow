package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

public class DefaultModelTest extends FlowTestCase {

    @Test
    public void testDefaultModel() {
        Delete.table(DefaultModel.class);
        DefaultModel defaultModel = new DefaultModel();
        defaultModel.setName("Test");
        defaultModel.save();
    }
}
