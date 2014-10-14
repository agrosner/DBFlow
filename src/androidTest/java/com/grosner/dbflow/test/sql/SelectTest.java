package com.grosner.dbflow.test.sql;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.sql.From;
import com.grosner.dbflow.sql.Join;
import com.grosner.dbflow.sql.Select;
import com.grosner.dbflow.sql.Where;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.structure.Column;
import com.grosner.dbflow.test.FlowTestCase;
import com.grosner.dbflow.test.structure.TestModel1;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class SelectTest extends FlowTestCase {
    @Override
    protected String getDBName() {
        return "select";
    }

    @Override
    protected void modifyConfiguration(DBConfiguration.Builder builder) {
        builder.addModelClasses(TestModel2.class);
    }

    public void testSelectStatement() {
        Where<TestModel1> where = new Select("name").from(TestModel1.class)
                .where(Condition.column("name").is("test"));

        assertEquals("SELECT name FROM TestModel1 WHERE name = 'test'", where.getQuery().trim());

        Where<TestModel2> where1 = new Select("name", "type").from(TestModel2.class)
                .where(Condition.column("name").is("test"),
                        Condition.column("type").is("test"));

        assertEquals("SELECT name, type FROM TestModel2 WHERE name = 'test' AND type = 'test'", where1.getQuery().trim());

        Where<TestModel2> where2 = new Select().distinct().from(TestModel2.class).where();

        assertEquals("SELECT DISTINCT * FROM TestModel2", where2.getQuery().trim());

        Where<TestModel2> where3 = new Select().count().from(TestModel2.class).where();

        assertEquals("SELECT COUNT(*)  FROM TestModel2", where3.getQuery().trim());
    }

    public void testJoins() {
        From<TestModel1> baseFrom = new Select().from(TestModel1.class);
        baseFrom.join(TestModel2.class, Join.JoinType.CROSS).on(Condition.column("TestModel1.name").is("TestModel2.name"));

        assertEquals("SELECT * FROM TestModel1 CROSS JOIN TestModel2 ON TestModel1.name = TestModel2.name", baseFrom.getQuery().trim());
    }

    private static class TestModel2 extends TestModel1 {
        @Column
        private String type;
    }
}
