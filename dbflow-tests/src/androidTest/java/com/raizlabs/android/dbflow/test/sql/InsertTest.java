package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
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
                .where(InsertModel_Table.name.is("Test3")).querySingle();
        assertNotNull(model);
    }

    public void testInsertAutoIncNotFirst() {
        Delete.table(InsertModelAutoIncPrimaryKeyNotFirst.class);

        InsertModelAutoIncPrimaryKeyNotFirst model = new InsertModelAutoIncPrimaryKeyNotFirst();
        model.value1 = "test1";
        model.value2 = "test2";

        model.save();

        model = new Select().from(InsertModelAutoIncPrimaryKeyNotFirst.class)
            .where(InsertModelAutoIncPrimaryKeyNotFirst_Table.value1.is("test1")).querySingle();
        assertNotNull(model);

    }

    @Table(database = TestDatabase.class)
    public static class InsertModelAutoIncPrimaryKeyNotFirst extends BaseModel {
        @Column
        String value1;

        @Column
        @PrimaryKey(autoincrement =  true)
        int id;

        @Column
        String value2;
    }
}
