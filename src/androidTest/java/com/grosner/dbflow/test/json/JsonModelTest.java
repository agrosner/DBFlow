package com.grosner.dbflow.test.json;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.structure.Column;
import com.grosner.dbflow.structure.ColumnType;
import com.grosner.dbflow.structure.json.JSONModel;
import com.grosner.dbflow.test.FlowTestCase;
import com.grosner.dbflow.test.structure.TestModel1;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class JsonModelTest extends FlowTestCase {
    @Override
    protected String getDBName() {
        return "jsonmodel";
    }

    @Override
    protected void modifyConfiguration(DBConfiguration.Builder builder) {
        builder.addModelClasses(TestJsonModelClass.class);
    }

    public void testJsonModel() {
        try {
            JSONObject jsonObject = new JSONObject("{" +
                    "name: test" +
                    "}");
            TestJsonModel<TestModel1> testJsonModel1 = new TestJsonModel<TestModel1>(TestModel1.class, jsonObject);
            testJsonModel1.save(false);

            assertTrue(testJsonModel1.exists());
            assertNotNull(testJsonModel1.toModel());

            jsonObject = new JSONObject("{" +
                    "name: test," +
                    "party_type: club," +
                    "count: 10" +
                    "}");

            TestJsonModel<TestJsonModelClass> testJsonModel = new TestJsonModel<TestJsonModelClass>(TestJsonModelClass.class, jsonObject);
            testJsonModel.save(false);

            assertTrue(testJsonModel.exists());
            assertNotNull(testJsonModel.toModel());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static class TestJsonModelClass extends TestModel1 {

        @Column(@ColumnType(ColumnType.PRIMARY_KEY))
        private String party_type;

        @Column
        private int count;

        @Column
        private String party_name;
    }

    private static class TestJsonModel<TestModel extends TestModel1> extends JSONModel<TestModel> {

        public TestJsonModel(Class<TestModel> testModelClass, JSONObject jsonObject) {
            super(jsonObject, testModelClass);
        }

        public TestJsonModel(Class<TestModel> testModelClass) {
            super(testModelClass);
        }

    }
}
