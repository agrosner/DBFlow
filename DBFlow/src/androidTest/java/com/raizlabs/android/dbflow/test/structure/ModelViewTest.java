package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import java.util.List;

/**
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
        testModel2.save();

        testModel2 = new TestModel2();
        testModel2.order = 5;
        testModel2.name = "View2";
        testModel2.save();

        List<TestModelView> testModelViews = new Select().from(TestModelView.class).queryList();
        assertTrue(!testModelViews.isEmpty());
        assertTrue(testModelViews.size() == 1);
    }

    public void testModelViewCursorList() {
        FlowCursorList<TestModelView> list = new FlowCursorList<>(true, TestModelView.class);
        assertNotNull(list.getItem(0));
    }

}
