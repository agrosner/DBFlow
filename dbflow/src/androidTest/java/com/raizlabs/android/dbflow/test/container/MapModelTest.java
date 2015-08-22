package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.structure.container.MapModel;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 */
public class MapModelTest extends FlowTestCase {

    public void testMapModel() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", "test");
        MapModel<TestModel1> model1MapModel = new MapModel<>(dataMap, TestModel1.class);
        model1MapModel.save();

        assertTrue(model1MapModel.exists());
        assertNotNull(model1MapModel.toModel());

        Map<String, Object> otherDataMap = new HashMap<>();
        otherDataMap.put("name", "test");
        otherDataMap.put("party_type", "club");
        otherDataMap.put("count1", 10);

        otherDataMap.put("testModel", dataMap);

        MapModel<TestModelContainerClass> testModelContainerClassMapModel = new MapModel<>(otherDataMap, TestModelContainerClass.class);
        testModelContainerClassMapModel.save();

        assertTrue(testModelContainerClassMapModel.exists());
        assertNotNull(testModelContainerClassMapModel.toModel());
        assertNotNull(testModelContainerClassMapModel.toModel().testModel);
    }

}
