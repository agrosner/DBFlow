package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.sql.language.Select;
import com.grosner.dbflow.sql.language.Where;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.structure.BaseModelView;
import com.grosner.dbflow.structure.ModelViewDefinition;
import com.grosner.dbflow.test.FlowTestCase;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelViewTest extends FlowTestCase {
    @Override
    protected String getDBName() {
        return "modelview";
    }

    @Override
    protected void modifyConfiguration(DBConfiguration.Builder builder) {
        builder.addModelClasses(TestModel2.class, TestModelView.class);
    }

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

    private static class TestModelView extends BaseModelView<TestModel2> {

    }

    public static class TestModelViewDefinition implements ModelViewDefinition<TestModelView,TestModel2> {

        public TestModelViewDefinition() {
            super();
        }

        @Override
        public Where<TestModel2> getWhere() {
            return new Select().from(TestModel2.class).where(Condition.column("model_order").greaterThan(5));
        }

        @Override
        public String getName() {
            return getModelViewClass().getSimpleName();
        }

        @Override
        public Class<TestModelView> getModelViewClass() {
            return TestModelView.class;
        }

        @Override
        public Class<TestModel2> getModelClass() {
            return TestModel2.class;
        }
    }
}
