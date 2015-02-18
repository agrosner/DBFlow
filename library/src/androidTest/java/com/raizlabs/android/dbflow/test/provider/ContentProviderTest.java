package com.raizlabs.android.dbflow.test.provider;

import android.net.Uri;
import android.test.ProviderTestCase2;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.provider.ContentUtils;
import com.raizlabs.android.dbflow.sql.language.Delete;

/**
 * Description:
 */
public class ContentProviderTest extends ProviderTestCase2<TestContentProvider$Provider> {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FlowManager.init(getContext());
    }

    public ContentProviderTest() {
        super(TestContentProvider$Provider.class, TestContentProvider.AUTHORITY);
    }

    public void testContentProviderUtils() {
        Delete.tables(NoteModel.class, ContentProviderModel.class);

        ContentProviderModel contentProviderModel = new ContentProviderModel();
        contentProviderModel.notes = "Test";
        Uri uri = ContentUtils.insert(getMockContentResolver(), TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel);
        assertEquals(TestContentProvider.ContentProviderModel.CONTENT_URI + "/" + contentProviderModel.id, uri.toString());
        assertTrue(contentProviderModel.exists());

        NoteModel noteModel = new NoteModel();
        noteModel.note = "Test";
        noteModel.contentProviderModel = contentProviderModel;
        uri = ContentUtils.insert(getMockContentResolver(), TestContentProvider.NoteModel.CONTENT_URI, noteModel);
        assertEquals(TestContentProvider.NoteModel.CONTENT_URI + "/" + noteModel.id, uri.toString());
        assertTrue(noteModel.exists());

        assertTrue(ContentUtils.delete(getMockContentResolver(), TestContentProvider.NoteModel.CONTENT_URI, noteModel) > 0);
        assertTrue(!noteModel.exists());

        assertTrue(ContentUtils.delete(getMockContentResolver(), TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel) > 0);
        assertTrue(!contentProviderModel.exists());

        Delete.tables(NoteModel.class, ContentProviderModel.class);
    }

    public void testContentProviderNative() {
        Delete.tables(NoteModel.class, ContentProviderModel.class);

        ContentProviderModel contentProviderModel = new ContentProviderModel();
        contentProviderModel.notes = "Test";
        contentProviderModel.insert(false);
        assertTrue(contentProviderModel.exists());

        contentProviderModel.notes = "NewTest";
        contentProviderModel.update(false);
        contentProviderModel.load();
        assertEquals("NewTest", contentProviderModel.notes);

        NoteModel noteModel = new NoteModel();
        noteModel.note = "Test";
        noteModel.contentProviderModel = contentProviderModel;
        noteModel.insert(false);

        noteModel.note = "NewTest";
        noteModel.update(false);
        noteModel.load();
        assertEquals("NewTest", noteModel.note);

        assertTrue(noteModel.exists());

        noteModel.delete(false);
        assertTrue(!noteModel.exists());

        contentProviderModel.delete(false);
        assertTrue(!contentProviderModel.exists());

        Delete.tables(NoteModel.class, ContentProviderModel.class);
    }



    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FlowManager.destroy();
    }
}
