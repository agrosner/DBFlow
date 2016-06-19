package com.raizlabs.android.dbflow.test.list;

import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class FlowQueryListTest extends FlowTestCase {


    @Test
    public void test_ensureValidateBuilder() {
        Transaction.Success success = new Transaction.Success() {
            @Override
            public void onSuccess(Transaction transaction) {

            }
        };
        Transaction.Error error = new Transaction.Error() {
            @Override
            public void onError(Transaction transaction, Throwable error) {

            }
        };
        FlowQueryList<TestModel1> queryList
                = new FlowQueryList.Builder<>(TestModel1.class)
                .success(success)
                .error(error)
                .cacheSize(50)
                .changeInTransaction(true)
                .build();
        assertEquals(success, queryList.success());
        assertEquals(error, queryList.error());
        assertEquals(true, queryList.cursorList().cachingEnabled());
        assertEquals(50, queryList.cursorList().cacheSize());
        assertTrue(queryList.changeInTransaction());

    }
}
