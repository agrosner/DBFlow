package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.structure.save
import org.junit.Test

class ManyToManyTest : BaseUnitTest() {

    @Test
    fun testCanCreateManyToMany() {
        database<TestDatabase> {
            val artist = Artist(name = "Andrew Grosner")
                .save(db)
                .getOrThrow()
            val song = Song(name = "Livin' on A Prayer")
                .save(db)
                .getOrThrow()
            val artistSong = Artist_Song(
                0,
                artist,
                song
            )
            assert(artistSong.save(db).isSuccess)
        }
    }
}