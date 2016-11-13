package com.raizlabs.android.dbflow.test.typeconverter;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */

public class TypeConverterPropertyTest extends FlowTestCase {

    @Test
    public void test_canConvertProperties() {

        Condition eq = TestType_Table.thisHasCustom.eq(true);
        assertEquals("`thisHasCustom`='1'", eq.getQuery());

        Condition eq1 = TestType_Table.thisHasCustom.invertProperty().eq("1");
        assertEquals("`thisHasCustom`='1'", eq1.getQuery());
    }
}
