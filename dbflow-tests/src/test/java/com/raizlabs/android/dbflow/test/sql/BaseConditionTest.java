package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.BaseCondition;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import org.junit.Test;

import java.util.Date;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Description: Tests a few methods of {@link BaseCondition}
 */
public class BaseConditionTest extends FlowTestCase {

    @Test
    public void test_canGetNull() {
        Object val = null;
        String str = BaseCondition.convertValueToString(val, false);
        assertEquals("NULL", str);
    }

    @Test
    public void test_canConvertNumber() {
        int num = 5;
        String str = BaseCondition.convertValueToString(num, false);
        assertEquals("5", str);
    }

    @Test
    public void test_typeConvertValue() {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        String str = BaseCondition.convertValueToString(date, false);
        assertEquals("" + time, str);
    }

    @Test
    public void test_baseModelQueriable() {
        BaseModelQueriable<TestModel1> queriable = SQLite.select()
            .from(TestModel1.class);
        String str = BaseCondition.convertValueToString(queriable, true);
        assertEquals("(SELECT * FROM `TestModel1`)", str);
    }

    @Test
    public void test_NameAlias() {
        NameAlias nameAlias = NameAlias.builder("Dog")
            .as("Cat").build();
        String str = BaseCondition.convertValueToString(nameAlias, false);
        assertEquals(str, "`Cat`");
    }

    @Test
    public void test_sqlCondition() {
        SQLCondition condition = column(
            new NameAlias
                .Builder("Dog")
                .build())
            .eq("Cat");
        String str = BaseCondition.convertValueToString(condition, false);
        assertEquals(str, "`Dog`='Cat'");
    }

    @Test
    public void test_query() {
        Query query = new Query() {
            @Override
            public String getQuery() {
                return "Query";
            }
        };
        String str = BaseCondition.convertValueToString(query, false);
        assertEquals("Query", str);
    }

    @Test
    public void test_Blob() {
        byte[] testBytes = "Bytes".getBytes();
        Blob blob = new Blob(testBytes);
        String str = BaseCondition.convertValueToString(blob, false);
        // both Blob and byte[] should produce same output.
        assertEquals(str, BaseCondition.convertValueToString(testBytes, false));
        assertTrue(str.startsWith("X"));
        str = new String(hexStringToByteArray(str.replace("X'", "").replace("'", "")));
        assertEquals("Bytes", str);
    }

    @Test
    public void test_string() {
        String string = "string";
        String str = BaseCondition.convertValueToString(string, false);
        assertEquals("'string'", str);
    }

    @Test
    public void test_canPassEmptyParam() {
        String empty = Condition.Operation.EMPTY_PARAM;
        String str = BaseCondition.convertValueToString(empty, false);
        assertEquals(Condition.Operation.EMPTY_PARAM, str);
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
