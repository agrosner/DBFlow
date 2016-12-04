package com.raizlabs.android.dbflow.test.provider

import android.content.ContentResolver
import android.net.Uri

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.structure.provider.ContentUtils
import com.raizlabs.android.dbflow.test.FlowTestCase

import org.junit.Before
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowContentResolver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue


/**
 * Description:
 */
class ContentProviderTest : FlowTestCase() {

    private val mockContentResolver: ContentResolver
        get() = RuntimeEnvironment.application.contentResolver

    @Before
    fun setUp() {
        val provider = TestContentProvider_Provider()
        provider.onCreate()

        ShadowContentResolver.registerProvider(TestContentProvider.AUTHORITY, provider)
    }

    @Test
    fun testContentProviderUtils() {
        Delete.tables(NoteModel::class.java, ContentProviderModel::class.java)

        val contentProviderModel = ContentProviderModel()
        contentProviderModel.notes = "Test"
        var uri = ContentUtils.insert(mockContentResolver, TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel)
        assertEquals(TestContentProvider.ContentProviderModel.CONTENT_URI.toString() + "/" + contentProviderModel.id, uri.toString())
        assertTrue(contentProviderModel.exists())

        contentProviderModel.notes = "NewTest"
        val update = ContentUtils.update(mockContentResolver, TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel)
        assertEquals(update.toLong(), 1)
        assertTrue(contentProviderModel.exists())
        contentProviderModel.load()
        assertEquals("NewTest", contentProviderModel.notes)

        val noteModel = NoteModel()
        noteModel.note = "Test"
        noteModel.contentProviderModel = contentProviderModel
        uri = ContentUtils.insert(mockContentResolver, TestContentProvider.NoteModel.CONTENT_URI, noteModel)
        assertEquals(TestContentProvider.NoteModel.CONTENT_URI.toString() + "/" + noteModel.id, uri.toString())
        assertTrue(noteModel.exists())

        assertTrue(ContentUtils.delete(mockContentResolver, TestContentProvider.NoteModel.CONTENT_URI, noteModel) > 0)
        assertTrue(!noteModel.exists())

        assertTrue(ContentUtils.delete(mockContentResolver, TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel) > 0)
        assertTrue(!contentProviderModel.exists())

        Delete.tables(NoteModel::class.java, ContentProviderModel::class.java)
    }

    @Test
    fun testContentProviderNative() {
        Delete.tables(NoteModel::class.java, ContentProviderModel::class.java)

        val contentProviderModel = ContentProviderModel()
        contentProviderModel.notes = "Test"
        contentProviderModel.insert()
        assertTrue(contentProviderModel.exists())

        contentProviderModel.notes = "NewTest"
        contentProviderModel.update()
        contentProviderModel.load()
        assertEquals("NewTest", contentProviderModel.notes)

        val noteModel = NoteModel()
        noteModel.note = "Test"
        noteModel.contentProviderModel = contentProviderModel
        noteModel.insert()

        noteModel.note = "NewTest"
        noteModel.update()
        noteModel.load()
        assertEquals("NewTest", noteModel.note)

        assertTrue(noteModel.exists())

        noteModel.delete()
        assertTrue(!noteModel.exists())

        contentProviderModel.delete()
        assertTrue(!contentProviderModel.exists())

        Delete.tables(NoteModel::class.java, ContentProviderModel::class.java)
    }

    @Test
    fun testSyncableModel() {
        Delete.table(TestSyncableModel::class.java)

        var testSyncableModel = TestSyncableModel()
        testSyncableModel.name = "Name"
        testSyncableModel.save()

        assertTrue(testSyncableModel.exists())

        testSyncableModel.name = "TestName"
        testSyncableModel.update()
        assertEquals(testSyncableModel.name, "TestName")

        testSyncableModel = Select().from(TestSyncableModel::class.java)
                .where(TestSyncableModel_Table.id.`is`(testSyncableModel.id)).querySingle()

        val fromContentProvider = TestSyncableModel()
        fromContentProvider.id = testSyncableModel.id
        fromContentProvider.load()

        assertEquals(fromContentProvider.name, testSyncableModel.name)
        assertEquals(fromContentProvider.id, testSyncableModel.id)

        testSyncableModel.delete()
        assertFalse(testSyncableModel.exists())

        Delete.table(TestSyncableModel::class.java)
    }

}