package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.config.databaseForTable
import com.dbflow5.structure.save
import org.junit.Assert.assertTrue
import org.junit.Test

class ManyToManyTest : BaseUnitTest() {

    @Test
    fun testCanCreateManyToMany() {
        databaseForTable<Artist> { db ->
            val artist = Artist(name = "Andrew Grosner")
            val song = Song(name = "Livin' on A Prayer")

            artist.save(db)
            song.save(db)

            val artistSong = Artist_Song(
                id = 0,
                artist, song
            )
            assertTrue(artistSong.save(db))
        }
    }
}