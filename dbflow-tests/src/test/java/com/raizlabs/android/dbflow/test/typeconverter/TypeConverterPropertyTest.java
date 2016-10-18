package com.raizlabs.android.dbflow.test.typeconverter;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */

public class TypeConverterPropertyTest extends FlowTestCase {

    @Test
    public void test_canConvertProperties() {

        Condition eq = TestType_Table.thisHasCustom.eq(true);
        Assert.assertEquals("`thisHasCustom`='1'", eq.getQuery());
    }
}
