package com.grosner.dbflow.test.structure;

import android.location.Location;
import android.test.AndroidTestCase;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.observer.ModelObserver;
import com.grosner.dbflow.sql.SqlUtils;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.structure.BaseModel;
import com.grosner.dbflow.structure.Column;
import com.grosner.dbflow.structure.ColumnType;
import com.grosner.dbflow.structure.Ignore;
import com.grosner.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DBStructureTest extends AndroidTestCase {

    private FlowManager mManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DBConfiguration.Builder configurationBuilder
                = new DBConfiguration.Builder().databaseName("dbstructure.db").databaseVersion(1);
        mManager = new FlowManager();
        mManager.initialize(getContext(), configurationBuilder.create());
    }

    // region Test Table Existence

    public void testModelsFound() {
        assertEquals(2, mManager.getStructure().getTableStructure().size());
    }


    public static class TestModel1 extends BaseModel{
        @Column(@ColumnType(ColumnType.PRIMARY_KEY))
        String id;
    }

    @Ignore
    private static class IgnoredModel extends TestModel1 {

    }


    // endregion Test Model Existence

    // region Test Primary Where Query

    public void testPrimaryWhereQuery() {
        ConditionQueryBuilder<TestPrimaryWhere> primaryWhere = mManager.getStructure().getPrimaryWhereQuery(TestPrimaryWhere.class);
        assertEquals(primaryWhere.getQuery(), "location = ? AND id = ?");
    }

    private static class TestPrimaryWhere extends TestModel1{
        @Column(@ColumnType(ColumnType.PRIMARY_KEY))
        private Location location;
    }

    // endregion Test Primary Where Query

    // region Test Model Observer

    public void testModelObserver() {
        List<ModelObserver<? extends Model>> modelObservers = mManager.getStructure().getModelObserverListForClass(TestModel1.class);
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
        testModel1.id = "Test";
        testModel1.setManager(mManager);
        testModel1.save(false);

        final TestModelObserver finalModel1Observer = model1Observer;
        TransactionManager.getInstance().processOnRequestHandler(1000, new Runnable() {
            @Override
            public void run() {
                assertTrue(finalModel1Observer.isCalled());
            }
        });
    }

    // endregion Test Model Observer

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
            assertEquals(model.id, "Test");

            called = true;
        }

        public boolean isCalled() {
            return called;
        }
    }
}
