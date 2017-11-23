package com.raizlabs.android.dbflow.contentprovider

import android.content.ContentResolver
import android.content.pm.ProviderInfo
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.from
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.sql.language.where
import com.raizlabs.android.dbflow.sql.queriable.result
import com.raizlabs.android.dbflow.structure.provider.ContentUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment

/**
 * Description:
 */
class ContentProviderTests : BaseUnitTest() {
    private val mockContentResolver: ContentResolver
        get() = RuntimeEnvironment.application.contentResolver

    @Before
    fun setUp() {
        val info = ProviderInfo()
        info.authority = TestContentProvider.AUTHORITY
        Robolectric.buildContentProvider(TestContentProvider_Provider::class.java).create(info)
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

        val contentProviderModel = ContentProviderModel(notes = "Test")
        contentProviderModel.insert()
        assertTrue(contentProviderModel.exists())

        contentProviderModel.notes = "NewTest"
        contentProviderModel.update()
        contentProviderModel.load()
        assertEquals("NewTest", contentProviderModel.notes)

        val noteModel = NoteModel(note = "Test", contentProviderModel = contentProviderModel)
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

        var testSyncableModel = TestSyncableModel(name = "Name")
        testSyncableModel.save()

        assertTrue(testSyncableModel.exists())

        testSyncableModel.name = "TestName"
        testSyncableModel.update()
        assertEquals(testSyncableModel.name, "TestName")

        testSyncableModel = (select from TestSyncableModel::class
                where (TestSyncableModel_Table.id.`is`(testSyncableModel.id))).result!!

        val fromContentProvider = TestSyncableModel(id = testSyncableModel.id)
        fromContentProvider.load()

        assertEquals(fromContentProvider.name, testSyncableModel.name)
        assertEquals(fromContentProvider.id, testSyncableModel.id)

        testSyncableModel.delete()
        assertFalse(testSyncableModel.exists())

        Delete.table(TestSyncableModel::class.java)
    }

}