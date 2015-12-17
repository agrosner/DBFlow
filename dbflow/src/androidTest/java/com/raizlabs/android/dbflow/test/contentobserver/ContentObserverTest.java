package com.raizlabs.android.dbflow.test.contentobserver;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;

public class ContentObserverTest extends FlowTestCase {

    public void testNotificationUri() {

        Uri notificationUri = SqlUtils.getNotificationUri(TestModel1.class, BaseModel.Action.SAVE, TestModel1_Table.name.getQuery(), "this is a %test");
        assertEquals(notificationUri.getAuthority(), FlowManager.getTableName(TestModel1.class));
        assertEquals(notificationUri.getFragment(), BaseModel.Action.SAVE.name());
        assertEquals(Uri.decode(notificationUri.getQueryParameter(Uri.encode(TestModel1_Table.name.getQuery()))), "this is a %test");
    }

    public void testContentObserver() {
        Delete.table(TestModel1.class);

        FlowContentObserver flowContentObserver = new FlowContentObserver();
        flowContentObserver.registerForContentChanges(getContext(), TestModel1.class);

        final Boolean[] methodcalled = {false, false, false, false};
        final Callable<Boolean>[] methodCalls = new Callable[4];
        for (int i = 0; i < methodCalls.length; i++) {
            methodCalls[i] = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return true;
                }
            };
        }

        FlowContentObserver.OnModelStateChangedListener onModelStateChangedListener = new FlowContentObserver.OnModelStateChangedListener() {
            @Override
            public void onModelStateChanged(Class<? extends Model> table, BaseModel.Action action, @NonNull SQLCondition[] conditions) {
                switch (action) {
                    case CHANGE:
                        for (int i = 0; i < methodCalls.length; i++) {
                            try {
                                methodcalled[i] = true;
                                methodCalls[i].call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case SAVE:
                        try {
                            methodcalled[0] = true;
                            methodCalls[0].call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case DELETE:
                        try {
                            methodcalled[1] = true;
                            methodCalls[1].call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case INSERT:
                        try {
                            methodcalled[2] = true;
                            methodCalls[2].call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case UPDATE:
                        try {
                            methodcalled[3] = true;
                            methodCalls[3].call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        };

        flowContentObserver.addModelChangeListener(onModelStateChangedListener);

        TestModel1 testModel1 = new TestModel1();
        testModel1.setName("Name");

        testModel1.insert();
        testModel1.update();
        testModel1.save();
        testModel1.delete();

        flowContentObserver.removeModelChangeListener(onModelStateChangedListener);

        // saved
        await().atMost(5, TimeUnit.SECONDS).until(methodCalls[0]);
        assertTrue(methodcalled[0]);


        // deleted
        await().atMost(5, TimeUnit.SECONDS).until(methodCalls[1]);
        assertTrue(methodcalled[1]);

        // inserted
        await().atMost(5, TimeUnit.SECONDS).until(methodCalls[2]);
        assertTrue(methodcalled[2]);

        // updated
        await().atMost(5, TimeUnit.SECONDS).until(methodCalls[3]);
        assertTrue(methodcalled[3]);

        flowContentObserver.unregisterForContentChanges(getContext());

        Delete.table(TestModel1.class);
    }

    public void testContentObserverTransaction() {
        Delete.table(TestModel1.class);

        FlowContentObserver flowContentObserver = new FlowContentObserver();
        flowContentObserver.registerForContentChanges(getContext(), TestModel1.class);
        flowContentObserver.setNotifyAllUris(true);
        final Callable<Boolean>[] methodCalls = new Callable[4];
        for (int i = 0; i < methodCalls.length; i++) {
            methodCalls[i] = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return true;
                }
            };
        }

        final boolean[] methodcalled = new boolean[]{false, false, false, false};
        FlowContentObserver.OnModelStateChangedListener onModelStateChangedListener = new FlowContentObserver.OnModelStateChangedListener() {
            @Override
            public void onModelStateChanged(@Nullable Class<? extends Model> table, BaseModel.Action action, @NonNull SQLCondition[] conditions) {
                switch (action) {
                    case CHANGE:
                        for (int i = 0; i < methodCalls.length; i++) {
                            try {
                                methodcalled[i] = true;
                                methodCalls[i].call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case SAVE:
                        try {
                            methodcalled[0] = true;
                            methodCalls[0].call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case DELETE:
                        try {
                            methodcalled[1] = true;
                            methodCalls[1].call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case INSERT:
                        try {
                            methodcalled[2] = true;
                            methodCalls[2].call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case UPDATE:
                        try {
                            methodcalled[3] = true;
                            methodCalls[3].call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        };

        flowContentObserver.addModelChangeListener(onModelStateChangedListener);

        TestModel1 testModel1 = new TestModel1();
        testModel1.setName("Name");

        flowContentObserver.beginTransaction();

        testModel1.insert();
        testModel1.update();
        testModel1.save();
        testModel1.delete();

        // not saved
        assertFalse(methodcalled[0]);

        // not deleted
        assertFalse(methodcalled[1]);

        // not inserted
        assertFalse(methodcalled[2]);

        // not updated
        assertFalse(methodcalled[3]);

        flowContentObserver.endTransactionAndNotify();


        // saved
        await().atMost(5, TimeUnit.SECONDS).until(methodCalls[0]);
        assertTrue(methodcalled[0]);

        // deleted
        await().atMost(5, TimeUnit.SECONDS).until(methodCalls[1]);
        assertTrue(methodcalled[1]);

        // inserted
        await().atMost(5, TimeUnit.SECONDS).until(methodCalls[2]);
        assertTrue(methodcalled[2]);

        // updated
        await().atMost(5, TimeUnit.SECONDS).until(methodCalls[3]);
        assertTrue(methodcalled[3]);

        flowContentObserver.removeModelChangeListener(onModelStateChangedListener);

        flowContentObserver.unregisterForContentChanges(getContext());

        Delete.table(TestModel1.class);
    }
}
