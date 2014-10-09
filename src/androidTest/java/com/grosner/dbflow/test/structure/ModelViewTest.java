package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.sql.Select;
import com.grosner.dbflow.sql.Where;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.structure.BaseModelView;
import com.grosner.dbflow.structure.Column;
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

    public void testModelView() {
        TestModel2 testModel2 = new TestModel2();
        testModel2.order = 6;
        testModel2.name = "View";
        testModel2.setManager(mManager);
        testModel2.save(false);

        TransactionManager transactionManager = new TransactionManager(mManager, "ModelViewTest", false);

        List<TestModelView> testModelViews = transactionManager.selectAllFromTable(TestModelView.class);
        assertTrue(!testModelViews.isEmpty());

    }

    private static class TestModel2 extends TestModel1 {
        @Column(name = "model_order")
        private int order;
    }

    private class TestModelView extends BaseModelView<TestModel2> {

    }

    public static class TestModelViewDefinition extends ModelViewDefinition<TestModelView,TestModel2> {

        public TestModelViewDefinition(FlowManager flowManager) {
            super(flowManager);
        }

        @Override
        public Where<TestModel2> getWhere() {
            return new Select(mManager).from(TestModel2.class).where(Condition.column("model_order").greaterThan(5));
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
