package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.structure.container.JSONModel;
import com.raizlabs.android.dbflow.structure.container.MapModelContainer;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

import static com.raizlabs.android.dbflow.test.structure.TestModel1_Table.name;
import static com.raizlabs.android.dbflow.test.structure.TestModel2_Table.model_order;
import static org.junit.Assert.assertTrue;

public class ComplexModelTest extends FlowTestCase {

    @Test
    public void testComplexModel() {
        ComplexModel complexModel = new ComplexModel();

        complexModel.name = "Test";

        JSONModel<TestModel1> jsonModel = new JSONModel<>(TestModel1.class);
        jsonModel.put(name, "Test");
        jsonModel.save();

        complexModel.testModel1 = jsonModel;

        MapModelContainer<TestModel2> mapModelContainer = new MapModelContainer<>(TestModel2.class);
        mapModelContainer.put(name, "Test1");
        mapModelContainer.put(model_order, 1);
        mapModelContainer.save();

        complexModel.mapModelContainer = mapModelContainer;

        complexModel.save();


        assertTrue(complexModel.exists());

        complexModel.delete();
        assertTrue(!complexModel.exists());

        jsonModel.delete();
        mapModelContainer.delete();

        assertTrue(!jsonModel.exists());
        assertTrue(!mapModelContainer.exists());
    }

}

