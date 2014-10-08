package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.observer.ModelObserver;

import junit.framework.Assert;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
public class TestModelObserver implements ModelObserver<DBStructureTest.TestModel1> {

    @Override
    public Class<DBStructureTest.TestModel1> getModelClass() {
        return DBStructureTest.TestModel1.class;
    }

    @Override
    public void onModelChanged(FlowManager flowManager, DBStructureTest.TestModel1 model, Mode mode) {
        Assert.assertEquals(mode, Mode.DEFAULT);
    }
}
