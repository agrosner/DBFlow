package com.raizlabs.android.dbflow.test.structure.backup;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class BackupTest extends FlowTestCase {

    @Test
    public void testBackup() {

        Delete.table(BackupModel.class);

        BackupModel backupModel = new BackupModel();
        backupModel.name = "Test";
        backupModel.save();

        assertTrue(backupModel.exists());

        Delete.table(BackupModel.class);

    }
}
