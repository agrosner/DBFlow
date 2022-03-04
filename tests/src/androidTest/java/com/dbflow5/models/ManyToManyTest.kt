package com.dbflow5.models

import com.dbflow5.TestDatabase_Database
import com.dbflow5.artistAdapter
import com.dbflow5.artistSongAdapter
import com.dbflow5.songAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Rule
import org.junit.Test

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