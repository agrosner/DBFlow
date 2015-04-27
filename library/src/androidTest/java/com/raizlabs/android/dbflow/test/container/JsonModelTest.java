package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.container.JSONArrayModel;
import com.raizlabs.android.dbflow.structure.container.JSONModel;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1$Table;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description:
 */
public class JsonModelTest extends FlowTestCase {

    public void testJsonModel() {

        Delete.tables(TestModelContainerClass.class, ParentModel.class);

        try {
            JSONObject jsonObject = new JSONObject("{" +
                    "name: testModel" +
                    "}");
            JSONModel<ParentModel> testJsonModel1 = new JSONModel<>(jsonObject, ParentModel.class);
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
                    "}, " +
                    "party_name: null" +
                    "}");

            JSONModel<TestModelContainerClass> testJsonModel = new JSONModel<>(jsonObject, TestModelContainerClass.class);
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

        Delete.tables(TestModelContainerClass.class, ParentModel.class);
    }

    public void testJsonArrayModel() {

        Delete.tables(ParentModel.class);

        try {
            JSONObject jsonObject = new JSONObject("{" +
                    "name: testModel" +
                    "}");
            JSONArrayModel<ParentModel> jsonArrayModel = new JSONArrayModel<>(ParentModel.class);
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

            for (int i = 0; i < jsonArrayModel.length(); i++) {
                assertTrue(jsonArrayModel.exists(i));
            }

            jsonArrayModel.delete(false);

            for (int i = 0; i < jsonArrayModel.length(); i++) {
                assertFalse(jsonArrayModel.exists(i));
            }

            assertTrue(jsonArrayModel.length() == 4);

            ParentModel firstModel = jsonArrayModel.getModelObject(0);
            assertTrue(firstModel.name.equals("testModel"));

            JSONModel<ParentModel> firstJsonModel = jsonArrayModel.getJsonModel(0);
            assertTrue(firstJsonModel.getValue(TestModel1$Table.NAME).equals("testModel"));

            Delete.tables(ParentModel.class);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void testNullForeignKey() throws JSONException {

        Delete.tables(TestModelContainerClass.class, ParentModel.class);

        JSONObject jsonObject = new JSONObject("{" +
                                               "name: test," +
                                               "party_type: club," +
                                               "count1: 10," +
                                               "isHappy: true," +
                                               "party_name: null" +
                                               "}");
        JSONModel<TestModelContainerClass> jsonModel = new JSONModel<>(jsonObject, TestModelContainerClass.class);
        jsonModel.save(false);
    }

}
