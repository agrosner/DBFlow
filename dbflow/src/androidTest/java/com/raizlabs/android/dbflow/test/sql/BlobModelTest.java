/*
package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

*/
/**
 * Description:
 *//*

public class BlobModelTest extends FlowTestCase {

    private static final String TEST_BLOB = "This is a test";

    public void testBlob() {

        BlobModel blobModel = new BlobModel();
        blobModel.blob = new Blob(TEST_BLOB.getBytes());
        blobModel.save();

        assertTrue(blobModel.exists());

        BlobModel model = new Select().from(BlobModel.class)
                .where(BlobModel_Table.key.is(blobModel.key))
                .querySingle();

        assertNotNull(model);
        assertNotNull(model.blob);
        assertEquals(new String(model.blob.getBlob()), TEST_BLOB);
    }
}
*/
