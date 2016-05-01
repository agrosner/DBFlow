package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table;
import com.raizlabs.android.dbflow.test.structure.TestModel2;
import com.raizlabs.android.dbflow.test.structure.TestModel2_Table;

import org.junit.Test;

import java.util.List;

import static com.raizlabs.android.dbflow.test.sql.TestModel3_Table.name;
import static com.raizlabs.android.dbflow.test.sql.TestModel3_Table.type;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SelectTest extends FlowTestCase {

    @Test
    public void testSimpleSelectStatement() {
        Where<TestModel1> where = new Select(name).from(TestModel1.class)
                .where(name.is("test"));

        assertEquals("SELECT `name` FROM `TestModel1` WHERE `name`='test'", where.getQuery().trim());
        where.query();

        Where<TestModel3> where4 = new Select().from(TestModel3.class)
                .where(name.eq("test"))
                .and(type.is("test"));

        assertEquals("SELECT * FROM `TestModel32` WHERE `name`='test' AND `type`='test'", where4.getQuery().trim());


    }

    @Test
    public void test_multipleProjectionAndSelection() {
        Where<TestModel3> where1 = new Select(name, type).from(TestModel3.class)
                .where(name.is("test"),
                        type.is("test"));

        assertEquals("SELECT `name`,`type` FROM `TestModel32` WHERE `name`='test' AND `type`='test'", where1.getQuery().trim());
    }

    @Test
    public void test_distinctClause() {
        Where<TestModel3> where2 = new Select().distinct().from(TestModel3.class).where();

        assertEquals("SELECT DISTINCT * FROM `TestModel32`", where2.getQuery().trim());
        where2.query();
    }

    @Test
    public void test_countClause() {
        Where<TestModel3> where3 = new Select(Method.count()).from(TestModel3.class).where();

        assertEquals("SELECT COUNT(*) FROM `TestModel32`", where3.getQuery().trim());
        where3.query();

        Where<TestModel3> where6 = new Select(Method.count(type))
                .from(TestModel3.class)
                .orderBy(name, true)
                .orderBy(type, true);
        assertEquals("SELECT COUNT(`type`) FROM `TestModel32` ORDER BY `name` ASC,`type` ASC", where6.getQuery().trim());

    }

    @Test
    public void test_maxSelect() {
        String methodQuery = SQLite.select(Method.max(TestModel3_Table.type).as("troop"))
                .from(TestModel3.class).getQuery();
        assertEquals("SELECT MAX(`type`) AS `troop` FROM `TestModel32`", methodQuery.trim());
    }

    @Test
    public void test_nestedSelect() {
        String query = SQLite.select()
                .from(TestModel3.class)
                .where(TestModel3_Table.type
                        .in(SQLite.select(TestModel2_Table.name)
                                .from(TestModel2.class)
                                .where(TestModel2_Table.name.is("Test")))).getQuery();
        assertEquals("SELECT * FROM `TestModel32` WHERE `type` IN " +
                "(SELECT `name` FROM `TestModel2` WHERE `name`='Test' )", query.trim());
    }


    @Test
    public void test_complicatedSum() {
        String operationalQuery = SQLite.select(new Method(Method.sum(TestModel3_Table.name))
                .minus(Method.sum(TestModel3_Table.type)).as("troop"), TestModel3_Table.type)
                .from(TestModel3.class).getQuery();

        assertEquals("SELECT (SUM(`name`) - SUM(`type`)) AS `troop`,`type` FROM `TestModel32`", operationalQuery.trim());
    }

    @Test
    public void test_withTableAs() {
        String query = SQLite.select(TestModel1_Table.name.withTable().as("program_id")).getQuery();
        assertEquals("SELECT `TestModel`.`name` AS `program_id`", query.trim());
    }

    @Test
    public void testJoins() {

        TestModel1 testModel1 = new TestModel1();
        testModel1.setName("Test");
        testModel1.save();

        TestModel3 testModel2 = new TestModel3();
        testModel2.setName("Test");
        testModel2.save();

        From<TestModel1> baseFrom = new Select().from(TestModel1.class);
        baseFrom.join(TestModel3.class, Join.JoinType.CROSS).on(TestModel1_Table.name.withTable().eq(TestModel3_Table.name.withTable()));

        assertEquals("SELECT * FROM `TestModel1` CROSS JOIN `TestModel32` ON `TestModel1`.`name`=`TestModel32`.`name`", baseFrom.getQuery().trim());

        List<TestModel1> list = baseFrom.where().queryList();
        assertTrue(!list.isEmpty());

        Where<TestModel1> where = new Select().from(TestModel1.class).join(TestModel3.class, Join.JoinType.INNER).natural().where();
        assertEquals("SELECT * FROM `TestModel1` NATURAL INNER JOIN `TestModel32`", where.getQuery().trim());

        where.query();
    }

    @Test
    public void testNulls() {

        String nullable = null;
        String query = SQLite.select().from(TestModel1.class).where(TestModel1_Table.name.eq(nullable)).getQuery();
        assertEquals("SELECT * FROM `TestModel1` WHERE `name`=NULL", query.trim());
    }

}
