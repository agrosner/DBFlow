package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
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
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import org.junit.Test;

/**
 * Description:
 */
public class ForeignKeyModelTest extends FlowTestCase {


    @Test
    public void testInsertAndUpdate() {

        Delete.table(FkParent.class);
        Delete.table(FkRelated.class);

        // Test insert
        FkParent parent = new FkParent();
        parent.related = new FkRelated();
        parent.save();

        // Test update parent with new related
        parent.related = new FkRelated();
        parent.save();
    }

    @Table(database = TestDatabase.class)
    public static class FkParent extends BaseModel {
        @Column
        @PrimaryKey(autoincrement = true)
        int id;

        @Column
        @ForeignKey(onUpdate = ForeignKeyAction.CASCADE, onDelete = ForeignKeyAction.CASCADE, saveForeignKeyModel = true)
        FkRelated related;
    }

    @Table(database = TestDatabase.class)
    public static class FkRelated extends BaseModel {
        @Column
        @PrimaryKey(autoincrement = true)
        int id;

        @Column
        String value;
    }
}
