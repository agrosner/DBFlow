package com.raizlabs.android.dbflow.test.utils;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Description:
 */
public class GenerationUtils {

    /**
     * Creates a random list of models for testing batch interactions.
     *
     * @param size
     * @return
     */
    public static <TestClass extends TestModel1> List<TestClass> generateRandomModels(Class<TestClass> testClass, int size) {
        List<TestClass> testModel1s = new ArrayList<>();
        TestClass testModel1;
        for (int i = 0; i < size; i++) {
            testModel1 = FlowManager.getModelAdapter(testClass).newInstance();
            testModel1.setName(UUID.randomUUID().toString());
            testModel1.save();
            testModel1s.add(testModel1);
        }

        return testModel1s;
    }

    public static List<TestModel1> generateRandomModels(int size) {
        return generateRandomModels(TestModel1.class, size);
    }
}
