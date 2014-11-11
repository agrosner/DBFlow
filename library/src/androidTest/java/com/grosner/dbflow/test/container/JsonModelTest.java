package com.grosner.dbflow.test.container;

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

    public void testJsonModel() {
        try {
            JSONObject jsonObject = new JSONObject("{" +
                    "name: testModel" +
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

            testJsonModel.delete(false);
            testJsonModel1.delete(false);
            assertFalse(testJsonModel1.exists());
            assertFalse(testJsonModel.exists());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
