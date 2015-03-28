package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.structure.container.JSONModel;
import com.raizlabs.android.dbflow.structure.container.MapModel;
import com.raizlabs.android.dbflow.test.FlowTestCase;

/**
 * Description:
 */
public class ComplexModelTest extends FlowTestCase {

    public void testComplexModel() {
        ComplexModel complexModel = new ComplexModel();

        complexModel.name = "Test";

        JSONModel<TestModel1> jsonModel = new JSONModel<TestModel1>(TestModel1.class);
        jsonModel.put(TestModel1$Table.NAME, "Test");

        complexModel.testModel1 = jsonModel;

        MapModel<TestModel2> mapModel = new MapModel<TestModel2>(TestModel2.class);
        mapModel.put(TestModel2$Table.NAME, "Test1");
        mapModel.put(TestModel2$Table.MODEL_ORDER, 1);

        complexModel.mapModel = mapModel;

        complexModel.save();


        assertTrue(complexModel.exists());

        complexModel.delete();
        assertTrue(!complexModel.exists());

        jsonModel.delete();
        mapModel.delete();

        assertTrue(!jsonModel.exists());
        assertTrue(!mapModel.exists());
    }

}

