package com.grosner.dbflow.test.container;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.structure.container.JSONModel;
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
        builder.addModelClasses(TestModelContainerClass.class);
    }

    public void testJsonModel() {
        try {
            JSONObject jsonObject = new JSONObject("{" +
                    "name: test" +
                    "}");
            JSONModel<TestModel1> testJsonModel1 = new JSONModel<TestModel1>(jsonObject, TestModel1.class);
            testJsonModel1.save(false);

            assertTrue(testJsonModel1.exists());
            assertNotNull(testJsonModel1.toModel());

            jsonObject = new JSONObject("{" +
                    "name: test," +
                    "party_type: club," +
                    "count: 10," +
                    "testModel: {" +
                    "name: testModel" +
                    "}" +
                    "}");

            JSONModel<TestModelContainerClass> testJsonModel = new JSONModel<TestModelContainerClass>(jsonObject, TestModelContainerClass.class);
            testJsonModel.save(false);

            assertTrue(testJsonModel.exists());
            assertNotNull(testJsonModel.toModel());
            assertNotNull(testJsonModel.toModel().testModel);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
