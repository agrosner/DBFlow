package com.raizlabs.android.dbflow.test.list;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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

    @Test
    public void test_canIterateQueryList() {
        List<TestModel1> models = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            TestModel1 model = new TestModel1();
            model.setName("" + i);
            models.add(model);
        }
        FlowManager.getDatabase(TestDatabase.class)
                .executeTransaction(FastStoreModelTransaction
                        .insertBuilder(FlowManager.getModelAdapter(TestModel1.class))
                        .addAll(models)
                        .build());

        List<TestModel1> queryList = SQLite.select()
                .from(TestModel1.class).flowQueryList();
        assertNotEquals(0, queryList.size());
        assertEquals(50, queryList.size());
        int count = 0;
        for (TestModel1 model : queryList) {
            assertEquals(count + "", model.getName());
            count++;
        }
    }
}
