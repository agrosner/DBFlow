package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.observer.ModelObserver;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.test.FlowTestCase;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelObserverTest extends FlowTestCase {
    @Override
    protected String getDBName() {
        return "modelobserver";
    }

    @Override
    protected void modifyConfiguration(DBConfiguration.Builder builder) {
        builder.addModelClasses(TestModel1.class);
    }

    // region Test Model Observer

    public void testModelObserver() {
        List<ModelObserver<? extends Model>> modelObservers = FlowManager.getManagerForTable(TestModel1.class).getModelObserverListForClass(TestModel1.class);
        assertNotNull(modelObservers);

        TestModelObserver model1Observer = null;
        for(ModelObserver modelObserver : modelObservers) {
            if(modelObserver.getClass().equals(TestModelObserver.class)) {
                model1Observer = (TestModelObserver) modelObserver;
                break;
            }
        }

        assertNotNull(model1Observer);

        TestModel1 testModel1 = new TestModel1();
        testModel1.name = "TestObserver";
        testModel1.save(false);

        final TestModelObserver finalModel1Observer = model1Observer;
        TransactionManager.getInstance().processOnRequestHandler(1000, new Runnable() {
            @Override
            public void run() {
                assertTrue(finalModel1Observer.isCalled());
            }
        });
    }


    /**
     * Author: andrewgrosner
     * Contributors: { }
     * Description:
     */
    public static class TestModelObserver implements ModelObserver<TestModel1> {

        private boolean called = false;

        @Override
        public Class<TestModel1> getModelClass() {
            return TestModel1.class;
        }

        @Override
        public void onModelChanged(FlowManager flowManager, TestModel1 model, Mode mode) {
            assertEquals(mode, Mode.DEFAULT);
            assertEquals(model.name, "TestObserver");

            called = true;
        }

        public boolean isCalled() {
            return called;
        }
    }

    // endregion Test Model Observer
}
