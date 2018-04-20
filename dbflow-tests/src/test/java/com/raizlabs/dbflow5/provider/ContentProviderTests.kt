package com.raizlabs.dbflow5.provider

import android.content.ContentResolver
import android.content.pm.ProviderInfo
import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.config.database
import com.raizlabs.dbflow5.query.Delete.Companion.table
import com.raizlabs.dbflow5.query.Delete.Companion.tables
import com.raizlabs.dbflow5.query.result
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.structure.exists
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        database(ContentDatabase::class) {

            tables(this, NoteModel::class, ContentProviderModel::class)

            var contentProviderModel = ContentProviderModel()
            contentProviderModel.notes = "Test"
            var uri = ContentUtils.insert(mockContentResolver, TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel)
            assertEquals(TestContentProvider.ContentProviderModel.CONTENT_URI.toString() + "/" + contentProviderModel.id, uri.toString())
            assertTrue(contentProviderModel.exists())

            contentProviderModel.notes = "NewTest"
            val update = ContentUtils.update(mockContentResolver, TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel)
            assertEquals(update.toLong(), 1)
            assertTrue(contentProviderModel.exists())
            contentProviderModel = contentProviderModel.load(this)!!
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

            tables(this, NoteModel::class, ContentProviderModel::class)
        }
    }

    @Test
    fun testContentProviderNative() {
        database(ContentDatabase::class) {
            tables(this, NoteModel::class, ContentProviderModel::class)

            var contentProviderModel = ContentProviderModel(notes = "Test")
            contentProviderModel.insert()
            assertTrue(contentProviderModel.exists())

            contentProviderModel.notes = "NewTest"
            contentProviderModel.update()
            contentProviderModel = contentProviderModel.load(this)!!
            assertEquals("NewTest", contentProviderModel.notes)

            var noteModel = NoteModel(note = "Test", contentProviderModel = contentProviderModel)
            noteModel.insert()

            noteModel.note = "NewTest"
            noteModel.update()
            noteModel = noteModel.load(this)!!
            assertEquals("NewTest", noteModel.note)

            assertTrue(noteModel.exists())

            noteModel.delete()
            assertTrue(!noteModel.exists())

            contentProviderModel.delete()
            assertTrue(!contentProviderModel.exists())

            tables(this, NoteModel::class, ContentProviderModel::class)
        }
    }

    @Test
    fun testSyncableModel() {
        database(ContentDatabase::class) {
            table(this, TestSyncableModel::class)

            var testSyncableModel = TestSyncableModel(name = "Name")
            testSyncableModel.save()

            assertTrue(testSyncableModel.exists())

            testSyncableModel.name = "TestName"
            testSyncableModel.update()
            assertEquals(testSyncableModel.name, "TestName")

            testSyncableModel = (select from TestSyncableModel::class
                where (TestSyncableModel_Table.id.`is`(testSyncableModel.id))).result!!

            var fromContentProvider = TestSyncableModel(id = testSyncableModel.id)
            fromContentProvider = fromContentProvider.load(this)!!

            assertEquals(fromContentProvider.name, testSyncableModel.name)
            assertEquals(fromContentProvider.id, testSyncableModel.id)

            testSyncableModel.delete()
            assertFalse(testSyncableModel.exists())

            table(this, TestSyncableModel::class)
        }
    }

}
