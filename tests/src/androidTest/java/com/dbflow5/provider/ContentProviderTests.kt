package com.dbflow5.provider

import android.content.ContentResolver
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.provider.ProviderTestRule
import com.dbflow5.BaseUnitTest
import com.dbflow5.config.database
import com.dbflow5.query.select
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/**
 * Description:
 */
class ContentProviderTests : BaseUnitTest() {

    @get:Rule
    val contentProviderRule = ProviderTestRule.Builder(TestContentProvider_Provider::class.java, TestContentProvider.AUTHORITY).build()

    private val mockContentResolver: ContentResolver
        get() = ApplicationProvider.getApplicationContext<Context>().contentResolver

    @Test
    fun testContentProviderUtils() {
        database(ContentDatabase::class) { db ->
            listOf(NoteModel::class, ContentProviderModel::class).forEach {
                com.dbflow5.query.delete(it).executeUpdateDelete(db)
            }

            var contentProviderModel = ContentProviderModel()
            contentProviderModel.notes = "Test"
            var uri = ContentUtils.insert(mockContentResolver, TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel)
            assertEquals(TestContentProvider.ContentProviderModel.CONTENT_URI.toString() + "/" + contentProviderModel.id, uri.toString())
            assertTrue(contentProviderModel.exists(db))

            contentProviderModel.notes = "NewTest"
            val update = ContentUtils.update(mockContentResolver, TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel)
            assertEquals(update.toLong(), 1)
            assertTrue(contentProviderModel.exists(db))
            contentProviderModel = contentProviderModel.load(db)!!
            assertEquals("NewTest", contentProviderModel.notes)

            val noteModel = NoteModel()
            noteModel.note = "Test"
            noteModel.contentProviderModel = contentProviderModel
            uri = ContentUtils.insert(mockContentResolver, TestContentProvider.NoteModel.CONTENT_URI, noteModel)
            assertEquals(TestContentProvider.NoteModel.CONTENT_URI.toString() + "/" + noteModel.id, uri.toString())
            assertTrue(noteModel.exists(db))

            assertTrue(ContentUtils.delete(mockContentResolver, TestContentProvider.NoteModel.CONTENT_URI, noteModel) > 0)
            assertTrue(!noteModel.exists(db))

            assertTrue(ContentUtils.delete(mockContentResolver, TestContentProvider.ContentProviderModel.CONTENT_URI, contentProviderModel) > 0)
            assertTrue(!contentProviderModel.exists(db))

            listOf(NoteModel::class, ContentProviderModel::class).forEach {
                com.dbflow5.query.delete(it).executeUpdateDelete(db)
            }
        }
    }

    @Test
    fun testContentProviderNative() {
        database(ContentDatabase::class) { db ->
            listOf(NoteModel::class, ContentProviderModel::class).forEach { com.dbflow5.query.delete(it).executeUpdateDelete(db) }

            var contentProviderModel = ContentProviderModel(notes = "Test")
            contentProviderModel.insert(db)
            assertTrue(contentProviderModel.exists(db))

            contentProviderModel.notes = "NewTest"
            contentProviderModel.update(db)
            contentProviderModel = contentProviderModel.load(db)!!
            assertEquals("NewTest", contentProviderModel.notes)

            var noteModel = NoteModel(note = "Test", contentProviderModel = contentProviderModel)
            noteModel.insert(db)

            noteModel.note = "NewTest"
            noteModel.update(db)
            noteModel = noteModel.load(db)!!
            assertEquals("NewTest", noteModel.note)

            assertTrue(noteModel.exists(db))

            noteModel.delete(db)
            assertTrue(!noteModel.exists(db))

            contentProviderModel.delete(db)
            assertTrue(!contentProviderModel.exists(db))

            listOf(NoteModel::class, ContentProviderModel::class).forEach { com.dbflow5.query.delete(it).executeUpdateDelete(db) }
        }
    }

    @Test
    fun testSyncableModel() {
        database(ContentDatabase::class) { db ->
            com.dbflow5.query.delete<TestSyncableModel>().execute(db)

            var testSyncableModel = TestSyncableModel(name = "Name")
            testSyncableModel.save(db)

            assertTrue(testSyncableModel.exists(db))

            testSyncableModel.name = "TestName"
            testSyncableModel.update(db)
            assertEquals(testSyncableModel.name, "TestName")

            testSyncableModel = (select from TestSyncableModel::class
                    where (TestSyncableModel_Table.id.`is`(testSyncableModel.id))).querySingle(db)!!

            var fromContentProvider = TestSyncableModel(id = testSyncableModel.id)
            fromContentProvider = fromContentProvider.load(db)!!

            assertEquals(fromContentProvider.name, testSyncableModel.name)
            assertEquals(fromContentProvider.id, testSyncableModel.id)

            testSyncableModel.delete(db)
            assertFalse(testSyncableModel.exists(db))

            com.dbflow5.query.delete<TestSyncableModel>().execute(db)
        }
    }

}