package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
public class InsertTest extends FlowTestCase {


    public void testInsert() {

        Delete.table(InsertModel.class);

        Insert<InsertModel> insert = new Insert<>(InsertModel.class).orFail()
                .columns(InsertModel$Table.NAME, InsertModel$Table.VALUE).values("Test", "Test1");

        assertEquals("INSERT OR FAIL INTO InsertModel(name, value) VALUES('Test','Test1')", insert.getQuery());

        FlowManager.getDatabase(TestDatabase.NAME).getWritableDatabase().execSQL(insert.getQuery());

        InsertModel model = Select.byId(InsertModel.class, "Test");
        assertNotNull(model);

    }
}
