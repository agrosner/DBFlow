package com.raizlabs.android.dbflow.test.sqlcipher;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class CipherTest extends FlowTestCase {


    @Before
    protected void setUp() throws Exception {
        SQLiteDatabase.loadLibs(getContext());
    }

    @Test
    public void testCipherModel() {
        Delete.table(CipherModel.class);

        CipherModel model = new CipherModel();
        model.name = "name";
        model.save();

        assertTrue(model.exists());

        CipherModel retrieval = SQLite.select()
                .from(CipherModel.class)
                .where(CipherModel_Table.name.eq("name")).querySingle();
        assertTrue(retrieval.id == model.id);

        Delete.table(CipherModel.class);
    }
}
