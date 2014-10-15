package com.grosner.dbflow.test.list;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.list.FlowTableList;
import com.grosner.dbflow.test.FlowTestCase;
import com.grosner.dbflow.test.structure.TestModel1;
import com.grosner.dbflow.test.utils.GenerationUtils;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ListTest extends FlowTestCase {
    @Override
    protected String getDBName() {
        return "list";
    }

    @Override
    protected void modifyConfiguration(DBConfiguration.Builder builder) {

    }

    public void testTableList() {
        List<TestModel1> testModel1s = GenerationUtils.generateRandomModels(100);

        FlowTableList<TestModel1> flowTableList = new FlowTableList<TestModel1>(TestModel1.class);

        assertTrue(flowTableList.size()==100);

        assertTrue(flowTableList.containsAll(testModel1s));

        TestModel1 model1 = flowTableList.remove(0);

        assertTrue(flowTableList.size() == 99);

        assertTrue(flowTableList.add(model1));

        assertTrue(flowTableList.size() == 100);

        flowTableList.clear();

        assertTrue(flowTableList.size() == 0);
    }
}
