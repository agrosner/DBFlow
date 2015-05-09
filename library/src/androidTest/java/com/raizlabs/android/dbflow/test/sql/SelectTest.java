package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import java.util.List;

/**
 * Description:
 */
public class SelectTest extends FlowTestCase {

    public void testSelectStatement() {
        Where<TestModel1> where = new Select("name").from(TestModel1.class)
                .where(Condition.column("name").is("test"));

        assertEquals("SELECT `name` FROM `TestModel1` WHERE `name`='test'", where.getQuery().trim());
        where.query();

        Where<TestModel3> where1 = new Select("name", "type").from(TestModel3.class)
                .where(Condition.column("name").is("test"),
                        Condition.column("type").is("test"));

        assertEquals("SELECT `name`, `type` FROM `TestModel3` WHERE `name`='test' AND `type`='test'", where1.getQuery().trim());

        Where<TestModel3> where2 = new Select().distinct().from(TestModel3.class).where();

        assertEquals("SELECT DISTINCT * FROM `TestModel3`", where2.getQuery().trim());
        where2.query();

        Where<TestModel3> where3 = new Select().count().from(TestModel3.class).where();

        assertEquals("SELECT COUNT(*)  FROM `TestModel3`", where3.getQuery().trim());
        where3.query();


        Where<TestModel3> where4 = new Select().from(TestModel3.class)
                .where("`name`=?", "test")
                .and(Condition.column(TestModel3$Table.TYPE).is("test"));

        assertEquals("SELECT * FROM `TestModel3` WHERE `name`='test' AND `type`='test'", where4.getQuery().trim());
    }

    public void testJoins() {

        TestModel1 testModel1 = new TestModel1();
        testModel1.name = "Test";
        testModel1.save();

        TestModel3 testModel2 = new TestModel3();
        testModel2.name = "Test";
        testModel2.save();

        From<TestModel1> baseFrom = new Select().from(TestModel1.class);
        baseFrom.join(TestModel3.class, Join.JoinType.CROSS).on(Condition.column("TestModel1.name").is("TestModel3.name"));

        assertEquals("SELECT * FROM `TestModel1` CROSS JOIN `TestModel3` ON `TestModel1`.`name`=TestModel3.name", baseFrom.getQuery().trim());

        List<TestModel1> list = baseFrom.where().queryList();
        assertTrue(!list.isEmpty());

        Where<TestModel1> where = new Select().from(TestModel1.class).join(TestModel3.class, Join.JoinType.INNER).natural().where();
        assertEquals("SELECT * FROM `TestModel1` NATURAL INNER JOIN `TestModel3`", where.getQuery().trim());

        where.query();
    }

}
