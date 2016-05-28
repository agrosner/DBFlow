package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

import static com.raizlabs.android.dbflow.sql.language.Method.avg;
import static com.raizlabs.android.dbflow.sql.language.Method.cast;
import static com.raizlabs.android.dbflow.sql.language.Method.count;
import static com.raizlabs.android.dbflow.sql.language.Method.group_concat;
import static com.raizlabs.android.dbflow.sql.language.Method.max;
import static com.raizlabs.android.dbflow.sql.language.Method.min;
import static com.raizlabs.android.dbflow.sql.language.Method.sum;
import static com.raizlabs.android.dbflow.sql.language.Method.total;
import static com.raizlabs.android.dbflow.test.structure.TestModel1_Table.name;
import static org.junit.Assert.assertEquals;

/**
 * Description: Tests a {@link Method} class.
 */
public class MethodTest extends FlowTestCase {

    @Test
    public void test_avgMethod() {
        String query = avg(name).getQuery();
        assertEquals(query, "AVG(`name`)");
    }

    @Test
    public void test_countMethod() {
        String query = count(name).getQuery();
        assertEquals(query, "COUNT(`name`)");
    }

    @Test
    public void test_groupConcatMethod() {
        String query = group_concat(name).getQuery();
        assertEquals(query, "GROUP_CONCAT(`name`)");
    }

    @Test
    public void test_maxMethod() {
        String query = max(name).getQuery();
        assertEquals(query, "MAX(`name`)");
    }

    @Test
    public void test_minMethod() {
        String query = min(name).getQuery();
        assertEquals(query, "MIN(`name`)");
    }

    @Test
    public void test_sumMethod() {
        String query = sum(name).getQuery();
        assertEquals(query, "SUM(`name`)");
    }

    @Test
    public void test_totalMethod() {
        String query = total(name).getQuery();
        assertEquals(query, "TOTAL(`name`)");
    }

    @Test
    public void test_castMethod() {
        String query = cast(name).as(SQLiteType.INTEGER).getQuery();
        assertEquals("CAST(`name` AS INTEGER)", query);
    }
}
