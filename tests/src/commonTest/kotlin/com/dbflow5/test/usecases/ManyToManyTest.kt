package com.dbflow5.test.usecases

import com.dbflow5.test.Artist
import com.dbflow5.test.Artist_Song
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.Song
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.artistAdapter
import com.dbflow5.test.artistSongAdapter
import com.dbflow5.test.songAdapter
import kotlin.test.Test

class ManyToManyTest {

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
        artistSongAdapter.save(artistSong)
    }
}