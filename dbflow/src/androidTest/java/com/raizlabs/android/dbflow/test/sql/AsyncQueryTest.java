package com.raizlabs.android.dbflow.test.sql;

import android.database.Cursor;

import com.raizlabs.android.dbflow.runtime.transaction.TransactionListenerAdapter;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.structure.AsyncModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import com.raizlabs.android.dbflow.test.structure.TestModel1$Table;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description:
 */
public class AsyncQueryTest extends FlowTestCase {

    public void testAsyncQuery() {
        TestModel1 testModel1 = new TestModel1();
        testModel1.name = "Async";
        testModel1.save();

        new Select().from(TestModel1.class)
                .where(column(TestModel1$Table.NAME).is("Async"))
                .async().querySingle(new TransactionListenerAdapter<TestModel1>() {
            @Override
            public void onResultReceived(TestModel1 testModel1) {

            }
        });

        new Update<>(TestModel1.class)
                .set(column(TestModel1$Table.NAME).is("Async2"))
                .where(column(TestModel1$Table.NAME).is("Async"))
                .async().query(new TransactionListenerAdapter<Cursor>() {
            @Override
            public void onResultReceived(Cursor cursor) {

            }
        });

        testModel1.async().withListener(new AsyncModel.OnModelChangedListener() {
            @Override
            public void onModelChanged(Model model) {
                assertFalse(model.exists());
            }
        }).delete();

    }
}
