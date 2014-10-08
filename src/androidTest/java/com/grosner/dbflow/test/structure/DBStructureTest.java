package com.grosner.dbflow.test.structure;

import android.location.Location;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.observer.ModelObserver;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.structure.Column;
import com.grosner.dbflow.structure.ColumnType;
import com.grosner.dbflow.structure.ForeignKeyReference;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.test.FlowTestCase;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DBStructureTest extends FlowTestCase {

    @Override
    protected void modifyConfiguration(DBConfiguration.Builder builder) {
        builder.foreignKeysSupported();
    }

    @Override
    protected String getDBName() {
        return "dbstructure";
    }

    // region Test Primary Where Query

    public void testPrimaryWhereQuery() {
        ConditionQueryBuilder<TestPrimaryWhere> primaryWhere = mManager.getStructure().getPrimaryWhereQuery(TestPrimaryWhere.class);
        assertEquals(primaryWhere.getQuery(), "location = ? AND name = ?");
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
        testModel1.name = "Test";
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
            assertEquals(model.name, "Test");

            called = true;
        }

        public boolean isCalled() {
            return called;
        }
    }

    // endregion Test Model Observer

    // region Test Foreign Key & Converter

    public void testForeignKey() {
        TestModel1 testModel1 = new TestModel1();
        testModel1.name = "Test";
        testModel1.setManager(mManager);
        testModel1.save(false);

        ForeignModel foreignModel = new ForeignModel();
        foreignModel.testModel1 = testModel1;
        foreignModel.name = "Test";
        foreignModel.setManager(mManager);
        foreignModel.save(false);

        TransactionManager transactionManager = new TransactionManager(mManager, "Foreign Test", false);

        ForeignModel retrieved = transactionManager.selectModelById(ForeignModel.class, "Test");
        assertNotNull(retrieved);
        assertNotNull(retrieved.testModel1);
        assertEquals(retrieved.testModel1, foreignModel.testModel1);
    }

    private static class ForeignModel extends TestModel1 {
        @Column(value = @ColumnType(ColumnType.FOREIGN_KEY),
                references =
                        {@ForeignKeyReference(columnName = "testmodel_id",
                                foreignColumnName = "name",
                                columnType = String.class)})
        private TestModel1 testModel1;
    }

    // endregion Test Foreign Key & Converter
}
