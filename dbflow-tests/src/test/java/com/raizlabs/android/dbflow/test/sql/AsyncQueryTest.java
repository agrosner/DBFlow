package com.raizlabs.android.dbflow.test.sql;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.AsyncModel;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Description:
 */
public class AsyncQueryTest extends FlowTestCase {

    @Test
    public void testAsyncQuery() {
        TestModel1 testModel1 = new TestModel1();
        testModel1.setName("Async");
        testModel1.save();

        SQLite.select().from(TestModel1.class)
                .where(TestModel1_Table.name.is("Async"))
                .async()
                .queryResultCallback(new QueryTransaction.QueryResultCallback<TestModel1>() {
                    @Override
                    public void onQueryResult(QueryTransaction transaction, @NonNull CursorResult<TestModel1> tResult) {

                    }
                }).execute();

        SQLite.update(TestModel1.class)
                .set(TestModel1_Table.name.is("Async2"))
                .where(TestModel1_Table.name.is("Async"))
                .async().execute();

        testModel1.async().withListener(new AsyncModel.OnModelChangedListener<BaseModel>() {
            @Override
            public void onModelChanged(BaseModel model) {
                assertFalse(model.exists());
            }
        }).delete();

    }
}
