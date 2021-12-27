package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.config.databaseForTable
import com.dbflow5.structure.save
import org.junit.Test

class ManyToManyTest : BaseUnitTest() {

    @Test
    fun testCanCreateManyToMany() {
        databaseForTable<Artist> { db ->
            val artist = Artist(name = "Andrew Grosner")
                .save(db)
                .getOrThrow()
            val song = Song(name = "Livin' on A Prayer")
                .save(db)
                .getOrThrow()
            val artistSong = Artist_Song(
                id = 0,
                artist, song
            )
            assert(artistSong.save(db).isSuccess)
        }
    }
}