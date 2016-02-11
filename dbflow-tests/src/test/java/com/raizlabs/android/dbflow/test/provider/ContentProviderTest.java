package com.raizlabs.android.dbflow.test.provider;

import android.content.ContentResolver;
import android.net.Uri;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.provider.ContentUtils;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowContentResolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Description:
 */
public class ContentProviderTest extends FlowTestCase {

    private ContentResolver getMockContentResolver() {
        return RuntimeEnvironment.application.getContentResolver();
    }

    @Before
    public void setUp() {
        ShadowContentResolver.registerProvider(TestContentProvider.AUTHORITY, new TestContentProvider_Provider());
    }

    @Test
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

    @Test
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

    @Test
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
                .where(TestSyncableModel_Table.id.is(testSyncableModel.id)).querySingle();

        TestSyncableModel fromContentProvider = new TestSyncableModel();
        fromContentProvider.id = testSyncableModel.id;
        fromContentProvider.load();

        assertEquals(fromContentProvider.name, testSyncableModel.name);
        assertEquals(fromContentProvider.id, testSyncableModel.id);

        testSyncableModel.delete();
        assertFalse(testSyncableModel.exists());

        Delete.table(TestSyncableModel.class);
    }

}