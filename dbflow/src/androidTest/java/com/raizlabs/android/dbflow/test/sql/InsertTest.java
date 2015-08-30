/*
package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.TestDatabase;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

*/
/**
 * Description:
 *//*

public class InsertTest extends FlowTestCase {


    public void testInsert() {

        Delete.table(InsertModel.class);

        Insert<InsertModel> insert = Insert.into(InsertModel.class).orFail()
                .columns(InsertModel$Table.NAME, InsertModel$Table.VALUE).values("Test", "Test1");

        assertEquals("INSERT OR FAIL INTO `InsertModel`(`name`, `value`) VALUES('Test','Test1')", insert.getQuery());

        FlowManager.getDatabase(TestDatabase.NAME).getWritableDatabase().execSQL(insert.getQuery());

        InsertModel model = new Select().from(InsertModel.class)
                .where(column(InsertModel$Table.NAME).is("Test")).querySingle();
        assertNotNull(model);


        insert = Insert.into(InsertModel.class).orAbort()
                .values("Test2", "Test3");
        assertEquals("INSERT OR ABORT INTO `InsertModel` VALUES('Test2','Test3')", insert.getQuery());

        FlowManager.getDatabase(TestDatabase.NAME).getWritableDatabase().execSQL(insert.getQuery());

        model = new Select().from(InsertModel.class)
                .where(column(InsertModel$Table.NAME).is("Test2")).querySingle();
        assertNotNull(model);
    }
}
*/
