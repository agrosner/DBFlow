package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.structure.container.JSONArrayModel;
import com.raizlabs.android.dbflow.structure.container.JSONModel;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import com.raizlabs.android.dbflow.test.structure.TestModel1$Table;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description:
 */
public class JsonModelTest extends FlowTestCase {

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
                    "count1: 10," +
                    "isHappy: true," +
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

    public void testJsonArrayModel() {
        try {
            JSONObject jsonObject = new JSONObject("{" +
                    "name: testModel" +
                    "}");
            JSONArrayModel<TestModel1> jsonArrayModel = new JSONArrayModel<TestModel1>(TestModel1.class);
            jsonArrayModel.addJSONObject(jsonObject);

            jsonObject = new JSONObject("{" +
                    "name: testModel1" +
                    "}");

            jsonArrayModel.addJSONObject(jsonObject);

            jsonObject = new JSONObject("{" +
                    "name: testModel2" +
                    "}");
            jsonArrayModel.addJSONObject(jsonObject);

            jsonObject = new JSONObject("{" +
                    "name: testModel3" +
                    "}");
            jsonArrayModel.addJSONObject(jsonObject);

            jsonArrayModel.insert(false);
            jsonArrayModel.update(false);
            jsonArrayModel.save(false);

            for(int i = 0 ; i < jsonArrayModel.length(); i++) {
                assertTrue(jsonArrayModel.exists(i));
            }

            jsonArrayModel.delete(false);

            for(int i = 0 ; i < jsonArrayModel.length(); i++) {
                assertFalse(jsonArrayModel.exists(i));
            }

            assertTrue(jsonArrayModel.length() == 4);

            TestModel1 firstModel = jsonArrayModel.getModelObject(0);
            assertTrue(firstModel.name.equals("testModel"));

            JSONModel<TestModel1> firstJsonModel = jsonArrayModel.getJsonModel(0);
            assertTrue(firstJsonModel.getValue(TestModel1$Table.NAME).equals("testModel"));


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
