package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.structure.container.JSONModel;
import com.raizlabs.android.dbflow.structure.container.MapModel;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import static com.raizlabs.android.dbflow.test.structure.TestModel1_Table.*;
import static com.raizlabs.android.dbflow.test.structure.TestModel2_Table.model_order;

public class ComplexModelTest extends FlowTestCase {

    public void testComplexModel() {
        ComplexModel complexModel = new ComplexModel();

        complexModel.name = "Test";

        JSONModel<TestModel1> jsonModel = new JSONModel<>(TestModel1.class);
        jsonModel.put(name, "Test");

        complexModel.testModel1 = jsonModel;

        MapModel<TestModel2> mapModel = new MapModel<>(TestModel2.class);
        mapModel.put(name, "Test1");
        mapModel.put(model_order, 1);

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

