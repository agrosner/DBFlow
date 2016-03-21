package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.AsyncModel;
import com.raizlabs.android.dbflow.structure.Model;
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
                .async().query(null, null);

        SQLite.update(TestModel1.class)
                .set(TestModel1_Table.name.is("Async2"))
                .where(TestModel1_Table.name.is("Async"))
                .async().query(null, null);

        testModel1.async().withListener(new AsyncModel.OnModelChangedListener() {
            @Override
            public void onModelChanged(Model model) {
                assertFalse(model.exists());
            }
        }).delete();

    }
}
