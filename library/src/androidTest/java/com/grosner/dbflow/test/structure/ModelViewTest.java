package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.sql.language.Select;
import com.grosner.dbflow.test.FlowTestCase;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelViewTest extends FlowTestCase {

    /**
     * Tests to ensure the model view operates as expected
     */
    public void testModelView() {
        TestModel2 testModel2 = new TestModel2();
        testModel2.order = 6;
        testModel2.name = "View";
        testModel2.save(false);

        testModel2 = new TestModel2();
        testModel2.order = 5;
        testModel2.name = "View2";
        testModel2.save(false);

        TransactionManager transactionManager = new TransactionManager("ModelViewTest", false);

        List<TestModelView> testModelViews = Select.all(TestModelView.class);
        assertTrue(!testModelViews.isEmpty());
        assertTrue(testModelViews.size() == 1);
    }

}
