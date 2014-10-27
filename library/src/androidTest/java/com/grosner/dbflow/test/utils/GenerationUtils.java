package com.grosner.dbflow.test.utils;

import com.grosner.dbflow.test.structure.TestModel1;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class GenerationUtils {

    /**
     * Creates a random list of models for testing batch interactions.
     * @param size
     * @return
     */
    public static List<TestModel1> generateRandomModels(int size) {
        List<TestModel1> testModel1s = new ArrayList<TestModel1>();
        TestModel1 testModel1;
        for(int i = 0; i < 100; i++) {
            testModel1 = new TestModel1();
            testModel1.name = UUID.randomUUID().toString();
            testModel1.save(false);
            testModel1s.add(testModel1);
        }

        return testModel1s;
    }
}
