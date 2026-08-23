package com.dbflow5.test.usecases

import com.dbflow5.database.DatabaseObjectLookup
import com.dbflow5.test.Artist
import com.dbflow5.test.Artist_Song
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.Song
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.TestRule
import kotlin.test.Test

class ManyToManyTest : TestRule() {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testCanCreateManyToMany() = dbRule.runTest {
        val artistModel =
            artistAdapter.save(Artist(name = "Andrew Grosner"))
        val songModel =
            songAdapter.save(Song(name = "Livin' on A Prayer"))
        val artistSong = Artist_Song(
            0,
            artistModel,
            songModel
        )
        DatabaseObjectLookup.getModelAdapter(Artist_Song::class).save(artistSong)
    }
}