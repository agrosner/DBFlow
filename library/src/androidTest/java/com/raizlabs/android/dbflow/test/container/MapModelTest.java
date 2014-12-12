package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.structure.container.MapModel;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class MapModelTest extends FlowTestCase {

    public void testMapModel() {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("name", "test");
        MapModel<TestModel1> model1MapModel = new MapModel<TestModel1>(dataMap,TestModel1.class);
        model1MapModel.save(false);

        assertTrue(model1MapModel.exists());
        assertNotNull(model1MapModel.toModel());

        Map<String, Object> otherDataMap = new HashMap<String, Object>();
        otherDataMap.put("name", "test");
        otherDataMap.put("party_type", "club");
        otherDataMap.put("count1", 10);

        otherDataMap.put("testModel", dataMap);

        MapModel<TestModelContainerClass> testModelContainerClassMapModel = new MapModel<TestModelContainerClass>(otherDataMap, TestModelContainerClass.class);
        testModelContainerClassMapModel.save(false);

        assertTrue(testModelContainerClassMapModel.exists());
        assertNotNull(testModelContainerClassMapModel.toModel());
        assertNotNull(testModelContainerClassMapModel.toModel().testModel);
    }

}
