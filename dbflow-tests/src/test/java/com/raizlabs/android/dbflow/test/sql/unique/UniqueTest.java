package com.raizlabs.android.dbflow.test.sql.unique;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class UniqueTest extends FlowTestCase {

    UniqueModel uniqueModel;

    @Before
    public void createModel() {
        uniqueModel = new UniqueModel();
        uniqueModel.uniqueName = "This is unique";
        uniqueModel.anotherUnique = "This should rollback";
        uniqueModel.sharedUnique = "This is unique to both combos";
    }

    @Test
    public void testUniqueReplacesExisting() {
        uniqueModel.anotherUnique = System.currentTimeMillis() + "";// guarantee unique for this test.
        uniqueModel.insert();
        assertTrue(uniqueModel.exists());

        uniqueModel.insert();
        assertEquals(1, SQLite.selectCountOf().from(UniqueModel.class).count());
    }
}
