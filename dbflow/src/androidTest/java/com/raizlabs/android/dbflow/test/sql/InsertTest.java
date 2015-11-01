package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
public class InsertTest extends FlowTestCase {


    public void testInsert() {

        Delete.table(InsertModel.class);

        Insert<InsertModel> insert = SQLite.insert(InsertModel.class).orFail()
                .columnValues(InsertModel_Table.name.eq("Test"), InsertModel_Table.value.eq("Test1"));

        assertEquals("INSERT OR FAIL INTO `InsertModel`(`name`, `value`) VALUES('Test','Test1')", insert.getQuery());

        FlowManager.getDatabase(TestDatabase.NAME).getWritableDatabase().execSQL(insert.getQuery());

        InsertModel model = new Select().from(InsertModel.class)
                .where(InsertModel_Table.name.is("Test")).querySingle();
        assertNotNull(model);


        insert = SQLite.insert(InsertModel.class).orAbort()
                .values("Test2", "Test3");
        assertEquals("INSERT OR ABORT INTO `InsertModel` VALUES('Test2','Test3')", insert.getQuery());

        FlowManager.getDatabase(TestDatabase.NAME).getWritableDatabase().execSQL(insert.getQuery());

        model = new Select().from(InsertModel.class)
                .where(InsertModel_Table.name.is("Test2")).querySingle();
        assertNotNull(model);
    }
}
