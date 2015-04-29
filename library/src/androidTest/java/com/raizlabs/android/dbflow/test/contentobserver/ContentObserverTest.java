package com.raizlabs.android.dbflow.test.contentobserver;

import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description:
 */
public class ContentObserverTest extends FlowTestCase {

    public void testContentObserver() {
        Delete.table(TestModel1.class);

        FlowContentObserver flowContentObserver = new FlowContentObserver();
        flowContentObserver.registerForContentChanges(getContext(), TestModel1.class);

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

        testModel1.insert();
        testModel1.update();
        testModel1.save();
        testModel1.delete();

        flowContentObserver.removeModelChangeListener(modelChangeListener);

        // saved
        assertTrue(methodcalled[0]);


        // deleted
        assertTrue(methodcalled[1]);

        // inserted
        assertTrue(methodcalled[2]);

        // updated
        assertTrue(methodcalled[3]);

        flowContentObserver.unregisterForContentChanges(getContext());

        Delete.table(TestModel1.class);
    }
}
