package com.raizlabs.android.dbflow.test.contentobserver;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.concurrent.Callable;

/**
 * Description:
 */
public class MockOnModelStateChangedListener implements FlowContentObserver.OnModelStateChangedListener {

    final Boolean[] methodcalled = {false, false, false, false};
    final Callable<Boolean>[] methodCalls = new Callable[4];
    final SQLCondition[][] conditions = new SQLCondition[4][2];

    public MockOnModelStateChangedListener() {
        for (int i = 0; i < methodCalls.length; i++) {
            final int finalI = i;
            methodCalls[i] = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return methodcalled[finalI];
                }
            };
        }
    }

    public Callable<Boolean>[] getMethodCalls() {
        return methodCalls;
    }

    public Boolean[] getMethodcalled() {
        return methodcalled;
    }

    public SQLCondition[][] getConditions() {
        return conditions;
    }

    @Override
    public void onModelStateChanged(@Nullable Class<?> table, BaseModel.Action action, @NonNull SQLCondition[] primaryKeyValues) {
        switch (action) {
            case CHANGE:
                for (int i = 0; i < methodCalls.length; i++) {
                    try {
                        methodcalled[i] = true;
                        conditions[i] = primaryKeyValues;
                        methodCalls[i].call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case SAVE:
                try {
                    conditions[2] = primaryKeyValues;
                    methodcalled[2] = true;
                    methodCalls[2].call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DELETE:
                try {
                    conditions[3] = primaryKeyValues;
                    methodcalled[3] = true;
                    methodCalls[3].call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case INSERT:
                try {
                    conditions[0] = primaryKeyValues;
                    methodcalled[0] = true;
                    methodCalls[0].call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case UPDATE:
                try {
                    conditions[1] = primaryKeyValues;
                    methodcalled[1] = true;
                    methodCalls[1].call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
