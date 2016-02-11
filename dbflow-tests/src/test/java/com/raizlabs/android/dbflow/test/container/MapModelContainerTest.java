package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.structure.container.MapModelContainer;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class MapModelContainerTest extends FlowTestCase {

    @Test
    public void testMapModel() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", "test");
        MapModelContainer<TestModel1> model1MapModelContainer = new MapModelContainer<>(dataMap, TestModel1.class);
        model1MapModelContainer.save();

        assertTrue(model1MapModelContainer.exists());
        assertNotNull(model1MapModelContainer.toModel());

        Map<String, Object> otherDataMap = new HashMap<>();
        otherDataMap.put("name", "test");
        otherDataMap.put("party_type", "club");
        otherDataMap.put("count1", 10);

        otherDataMap.put("testModel", dataMap);

        MapModelContainer<TestModelContainerClass> testModelContainerClassMapModelContainer = new MapModelContainer<>(otherDataMap, TestModelContainerClass.class);
        testModelContainerClassMapModelContainer.save();

        assertTrue(testModelContainerClassMapModelContainer.exists());
        assertNotNull(testModelContainerClassMapModelContainer.toModel());
        assertNotNull(testModelContainerClassMapModelContainer.toModel().testModel);
    }

}
