package com.raizlabs.android.dbflow.test.provider;

import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;
import com.raizlabs.android.dbflow.structure.provider.BaseProviderModel;
import com.raizlabs.android.dbflow.structure.provider.ContentUtils;

/**
 * Description:
 */
@TableEndpoint(name = ContentProviderModel.NAME, contentProvider = ContentDatabase.class)
@Table(database = ContentDatabase.class, name = ContentProviderModel.NAME)
public class ContentProviderModel extends BaseProviderModel<ContentProviderModel> {

    public static final String NAME = "ContentProviderModel";

    @ContentUri(path = NAME, type = ContentUri.ContentType.VND_MULTIPLE + NAME)
    public static final Uri CONTENT_URI = ContentUtils.buildUriWithAuthority(ContentDatabase.AUTHORITY);

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String notes;

    @Column
    String title;

    @Override
    public Uri getDeleteUri() {
        return TestContentProvider.ContentProviderModel.CONTENT_URI;
    }

    @Override
    public Uri getInsertUri() {
        return TestContentProvider.ContentProviderModel.CONTENT_URI;
    }

    @Override
    public Uri getUpdateUri() {
        return TestContentProvider.ContentProviderModel.CONTENT_URI;
    }

    @Override
    public Uri getQueryUri() {
        return TestContentProvider.ContentProviderModel.CONTENT_URI;
    }
}
