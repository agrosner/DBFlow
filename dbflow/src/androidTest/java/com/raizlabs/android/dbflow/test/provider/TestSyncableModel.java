package com.raizlabs.android.dbflow.test.provider;

import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.provider.BaseSyncableProviderModel;

/**
 * Description:
 */
@Table(database = ContentDatabase.class)
public class TestSyncableModel extends BaseSyncableProviderModel<TestSyncableModel> {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;

    @Override
    public Uri getDeleteUri() {
        return TestContentProvider.TestSyncableModel.CONTENT_URI;
    }

    @Override
    public Uri getInsertUri() {
        return TestContentProvider.TestSyncableModel.CONTENT_URI;
    }

    @Override
    public Uri getUpdateUri() {
        return TestContentProvider.TestSyncableModel.CONTENT_URI;
    }

    @Override
    public Uri getQueryUri() {
        return TestContentProvider.TestSyncableModel.CONTENT_URI;
    }
}
