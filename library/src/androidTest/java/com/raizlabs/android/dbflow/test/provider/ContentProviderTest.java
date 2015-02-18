package com.raizlabs.android.dbflow.test.provider;

import com.raizlabs.android.dbflow.sql.ContentUtils;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.test.FlowTestCase;

/**
 * Description:
 */
public class ContentProviderTest extends FlowTestCase {

    public void testContentProvider() {
        Delete.tables(NoteModel.class,ContentProviderModel.class);

        ContentProviderModel contentProviderModel = new ContentProviderModel();
        contentProviderModel.notes = "Test";
        ContentUtils.insert(TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel);
        assertTrue(contentProviderModel.exists());

        NoteModel noteModel = new NoteModel();
        noteModel.note = "Test";
        noteModel.contentProviderModel = contentProviderModel;
        ContentUtils.insert(TestContentProvider.NoteModel.CONTENT_URI, noteModel);
        assertTrue(noteModel.exists());


        Delete.tables(NoteModel.class,ContentProviderModel.class);
    }
}
