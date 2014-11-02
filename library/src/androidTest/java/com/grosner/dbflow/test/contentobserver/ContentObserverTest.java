package com.grosner.dbflow.test.contentobserver;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.runtime.FlowContentObserver;
import com.grosner.dbflow.test.FlowTestCase;
import com.grosner.dbflow.test.structure.TestModel1;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ContentObserverTest extends FlowTestCase {

    @Override
    protected String getDBName() {
        return "contentobserver";
    }

    public void testContentObserver() {
        FlowContentObserver flowContentObserver = new FlowContentObserver();
        flowContentObserver.registerForContentChanges(TestModel1.class);

        final Boolean[] methodcalled = {false, false, false, false};
        FlowContentObserver.ModelChangeListener modelChangeListener = new FlowContentObserver.ModelChangeListener() {
            @Override
            public void onModelChanged() {
                for (int i = 0; i < methodcalled.length; i++) {
                    methodcalled[i] = true;
                }
            }

            @Override
            public void onModelSaved() {
                methodcalled[0] = true;
            }

            @Override
            public void onModelDeleted() {
                methodcalled[1] = true;
            }

            @Override
            public void onModelInserted() {
                methodcalled[2] = true;
            }

            @Override
            public void onModelUpdated() {
                methodcalled[3] = true;
            }
        };

        flowContentObserver.addModelChangeListener(modelChangeListener);

        TestModel1 testModel1 = new TestModel1();
        testModel1.name = "Name";

        testModel1.insert(false);
        testModel1.update(false);
        testModel1.save(false);
        testModel1.delete(false);

        flowContentObserver.removeModelChangeListener(modelChangeListener);

        // saved
        assertTrue(methodcalled[0]);


        // deleted
        assertTrue(methodcalled[1]);

        // inserted
        assertTrue(methodcalled[2]);

        // updated
        assertTrue(methodcalled[3]);

        flowContentObserver.unregisterForContentChanges();
    }
}
