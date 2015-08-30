/*
package com.raizlabs.android.dbflow.test.provider;

import android.net.Uri;
import android.test.ProviderTestCase2;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.provider.ContentUtils;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

*/
/**
 * Description:
 *//*

public class ContentProviderTest extends ProviderTestCase2<TestContentProvider_Provider> {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FlowManager.init(getContext());
    }

    public ContentProviderTest() {
        super(TestContentProvider_Provider.class, TestContentProvider.AUTHORITY);
    }

    public void testContentProviderUtils() {
        Delete.tables(NoteModel.class, ContentProviderModel.class);

        ContentProviderModel contentProviderModel = new ContentProviderModel();
        contentProviderModel.notes = "Test";
        Uri uri = ContentUtils.insert(getMockContentResolver(), TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel);
        assertEquals(TestContentProvider.ContentProviderModel.CONTENT_URI + "/" + contentProviderModel.id, uri.toString());
        assertTrue(contentProviderModel.exists());

        contentProviderModel.notes = "NewTest";
        int update = ContentUtils.update(getMockContentResolver(), TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel);
        assertEquals(update, 1);
        assertTrue(contentProviderModel.exists());
        contentProviderModel.load();
        assertEquals("NewTest", contentProviderModel.notes);

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
        contentProviderModel.insert();
        assertTrue(contentProviderModel.exists());

        contentProviderModel.notes = "NewTest";
        contentProviderModel.update();
        contentProviderModel.load();
        assertEquals("NewTest", contentProviderModel.notes);

        NoteModel noteModel = new NoteModel();
        noteModel.note = "Test";
        noteModel.contentProviderModel = contentProviderModel;
        noteModel.insert();

        noteModel.note = "NewTest";
        noteModel.update();
        noteModel.load();
        assertEquals("NewTest", noteModel.note);

        assertTrue(noteModel.exists());

        noteModel.delete();
        assertTrue(!noteModel.exists());

        contentProviderModel.delete();
        assertTrue(!contentProviderModel.exists());

        Delete.tables(NoteModel.class, ContentProviderModel.class);
    }

    public void testSyncableModel() {
        Delete.table(TestSyncableModel.class);

        TestSyncableModel testSyncableModel = new TestSyncableModel();
        testSyncableModel.name = "Name";
        testSyncableModel.save();

        assertTrue(testSyncableModel.exists());

        testSyncableModel.name = "TestName";
        testSyncableModel.update();
        assertEquals(testSyncableModel.name, "TestName");

        testSyncableModel = new Select().from(TestSyncableModel.class)
                    .where(column(TestSyncableModel_Table.ID).is(testSyncableModel.id)).querySingle();

        TestSyncableModel fromContentProvider = new TestSyncableModel();
        fromContentProvider.id = testSyncableModel.id;
        fromContentProvider.load();

        assertEquals(fromContentProvider.name, testSyncableModel.name);
        assertEquals(fromContentProvider.id, testSyncableModel.id);

        testSyncableModel.delete();
        assertFalse(testSyncableModel.exists());

        Delete.table(TestSyncableModel.class);
    }



    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FlowManager.destroy();
    }
}
*/
