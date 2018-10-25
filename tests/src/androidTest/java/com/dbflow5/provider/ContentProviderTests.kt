package com.dbflow5.provider

import android.content.ContentResolver
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.provider.ProviderTestRule
import com.dbflow5.BaseInstrumentedUnitTest
import com.dbflow5.config.database
import com.dbflow5.query.result
import com.dbflow5.query.select
import com.dbflow5.structure.exists
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/**
 * Description:
 */
class ContentProviderTests : BaseInstrumentedUnitTest() {

    @get:Rule
    val contentProviderRule = ProviderTestRule.Builder(TestContentProvider_Provider::class.java, TestContentProvider.AUTHORITY).build()

    private val mockContentResolver: ContentResolver
        get() = ApplicationProvider.getApplicationContext<Context>().contentResolver

    @Test
    fun testContentProviderUtils() {
        database(ContentDatabase::class) {
            listOf(NoteModel::class, ContentProviderModel::class).forEach {
                com.dbflow5.query.delete(it).executeUpdateDelete(this)
            }

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

            listOf(NoteModel::class, ContentProviderModel::class).forEach {
                com.dbflow5.query.delete(it).executeUpdateDelete(this)
            }
        }
    }

    @Test
    fun testContentProviderNative() {
        database(ContentDatabase::class) {
            listOf(NoteModel::class, ContentProviderModel::class).forEach { com.dbflow5.query.delete(it).executeUpdateDelete(this) }

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

            listOf(NoteModel::class, ContentProviderModel::class).forEach { com.dbflow5.query.delete(it).executeUpdateDelete(this) }
        }
    }

    @Test
    fun testSyncableModel() {
        database(ContentDatabase::class) {
            com.dbflow5.query.delete<TestSyncableModel>().execute(this)

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

            com.dbflow5.query.delete<TestSyncableModel>().execute(this)
        }
    }

}