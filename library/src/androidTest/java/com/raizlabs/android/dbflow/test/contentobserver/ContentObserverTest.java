package com.raizlabs.android.dbflow.test.contentobserver;

import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;
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

        FlowContentObserver.OnModelStateChangedListener onModelStateChangedListener = new FlowContentObserver.OnModelStateChangedListener() {
            @Override
            public void onModelStateChanged(Class<? extends Model> table, BaseModel.Action action) {
                switch (action) {
                    case CHANGE:
                        for (int i = 0; i < methodcalled.length; i++) {
                            methodcalled[i] = true;
                        }
                        break;
                    case SAVE:
                        methodcalled[0] = true;
                        break;
                    case DELETE:
                        methodcalled[1] = true;
                        break;
                    case INSERT:
                        methodcalled[2] = true;
                        break;
                    case UPDATE:
                        methodcalled[3] = true;
                        break;
                }
            }
        };

        flowContentObserver.addModelChangeListener(onModelStateChangedListener);

        TestModel1 testModel1 = new TestModel1();
        testModel1.name = "Name";

        testModel1.insert();
        testModel1.update();
        testModel1.save();
        testModel1.delete();

        flowContentObserver.removeModelChangeListener(onModelStateChangedListener);

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
