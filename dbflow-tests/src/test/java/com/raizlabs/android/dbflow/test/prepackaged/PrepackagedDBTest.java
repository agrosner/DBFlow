package com.raizlabs.android.dbflow.test.prepackaged;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class PrepackagedDBTest extends FlowTestCase {

    @Test
    public void test_canImportPrepackagedData() {

        List<Dog> list = SQLite.select()
                .from(Dog.class)
                .queryList();

        assertTrue(!list.isEmpty());
    }
}
