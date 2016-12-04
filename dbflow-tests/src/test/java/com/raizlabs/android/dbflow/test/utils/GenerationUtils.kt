package com.raizlabs.android.dbflow.test.utils

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.test.structure.TestModel1

import java.util.ArrayList
import java.util.UUID

/**
 * Description:
 */
object GenerationUtils {

    /**
     * Creates a random list of models for testing batch interactions.

     * @param size
     * *
     * @return
     */
    fun <TestClass : TestModel1> generateRandomModels(testClass: Class<TestClass>, size: Int): List<TestClass> {
        val testModel1s = ArrayList<TestClass>()
        var testModel1: TestClass
        for (i in 0..size - 1) {
            testModel1 = FlowManager.getModelAdapter(testClass).newInstance()
            testModel1.name = UUID.randomUUID().toString()
            testModel1s.add(testModel1)
        }

        return testModel1s
    }

    fun generateRandomModels(size: Int): List<TestModel1> {
        return generateRandomModels(TestModel1::class.java, size)
    }
}
