package com.raizlabs.android.dbflow.test.contentobserver;

import android.net.Uri;

import com.jayway.awaitility.Duration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContentObserverTest extends FlowTestCase {

    @Test
    public void testNotificationUri() {

        Uri notificationUri = SqlUtils.getNotificationUri(TestModel1.class, BaseModel.Action.SAVE, TestModel1_Table.name.getQuery(), "this is a %test");
        assertEquals(notificationUri.getAuthority(), FlowManager.getTableName(TestModel1.class));
        assertEquals(notificationUri.getFragment(), BaseModel.Action.SAVE.name());
        assertEquals(Uri.decode(notificationUri.getQueryParameter(Uri.encode(TestModel1_Table.name.getQuery()))), "this is a %test");
    }

    @Test
    public void testContentObserver() {
        Delete.table(TestModel1.class);

        FlowContentObserver flowContentObserver = new FlowContentObserver();
        flowContentObserver.registerForContentChanges(RuntimeEnvironment.application, TestModel1.class);


        MockOnModelStateChangedListener onModelStateChangedListener = new MockOnModelStateChangedListener();

        flowContentObserver.addModelChangeListener(onModelStateChangedListener);

        TestModel1 testModel1 = new TestModel1();
        testModel1.setName("Name");

        for (int i = 0; i < onModelStateChangedListener.getMethodCalls().length; i++) {
            if (i == 0) {
                testModel1.insert();
            } else if (i == 1) {
                testModel1.update();
            } else if (i == 2) {
                testModel1.save();
            } else {
                testModel1.delete();
            }
            await().timeout(Duration.FIVE_SECONDS).until(onModelStateChangedListener.getMethodCalls()[i]);
            assertTrue(onModelStateChangedListener.getMethodcalled()[i]);
        }

        flowContentObserver.removeModelChangeListener(onModelStateChangedListener);
        flowContentObserver.unregisterForContentChanges(RuntimeEnvironment.application);

        Delete.table(TestModel1.class);
    }

    @Test
    public void testContentObserverTransaction() {
        Delete.table(TestModel1.class);

        FlowContentObserver flowContentObserver = new FlowContentObserver();
        flowContentObserver.registerForContentChanges(getContext(), TestModel1.class);
        flowContentObserver.setNotifyAllUris(true);

        MockOnModelStateChangedListener mockOnModelStateChangedListener = new MockOnModelStateChangedListener();
        flowContentObserver.addModelChangeListener(mockOnModelStateChangedListener);

        TestModel1 testModel1 = new TestModel1();
        testModel1.setName("Name");

        flowContentObserver.beginTransaction();

        testModel1.insert();
        testModel1.update();
        testModel1.save();
        testModel1.delete();

        await().atMost(Duration.FIVE_SECONDS).ignoreExceptions().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return true;
            }
        });

        // not saved
        assertFalse(mockOnModelStateChangedListener.getMethodcalled()[0]);

        // not deleted
        assertFalse(mockOnModelStateChangedListener.getMethodcalled()[1]);

        // not inserted
        assertFalse(mockOnModelStateChangedListener.getMethodcalled()[2]);

        // not updated
        assertFalse(mockOnModelStateChangedListener.getMethodcalled()[3]);

        flowContentObserver.endTransactionAndNotify();

        await().atMost(5, TimeUnit.SECONDS).until(mockOnModelStateChangedListener.getMethodCalls()[0]);
        assertTrue(mockOnModelStateChangedListener.getMethodcalled()[0]);

        await().atMost(5, TimeUnit.SECONDS).until(mockOnModelStateChangedListener.getMethodCalls()[1]);
        assertTrue(mockOnModelStateChangedListener.getMethodcalled()[1]);

        await().atMost(5, TimeUnit.SECONDS).until(mockOnModelStateChangedListener.getMethodCalls()[2]);
        assertTrue(mockOnModelStateChangedListener.getMethodcalled()[2]);

        await().atMost(5, TimeUnit.SECONDS).until(mockOnModelStateChangedListener.getMethodCalls()[3]);
        assertTrue(mockOnModelStateChangedListener.getMethodcalled()[3]);

        flowContentObserver.removeModelChangeListener(mockOnModelStateChangedListener);

        flowContentObserver.unregisterForContentChanges(RuntimeEnvironment.application);

        Delete.table(TestModel1.class);
    }
}
