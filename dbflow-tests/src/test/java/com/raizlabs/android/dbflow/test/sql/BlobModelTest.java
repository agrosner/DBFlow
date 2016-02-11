package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BlobModelTest extends FlowTestCase {

    private static final String TEST_BLOB = "This is a test";

    public void testBlob() {

        BlobModel blobModel = new BlobModel();
        blobModel.setBlob(new Blob(TEST_BLOB.getBytes()));
        blobModel.save();

        assertTrue(blobModel.exists());

        BlobModel model = new Select().from(BlobModel.class)
                .where(BlobModel_Table.key.is(blobModel.key))
                .querySingle();

        assertNotNull(model);
        assertNotNull(model.getBlob());
        assertEquals(new String(model.getBlob().getBlob()), TEST_BLOB);
    }
}