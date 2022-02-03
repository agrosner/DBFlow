package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.artistAdapter
import com.dbflow5.artistSongAdapter
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.songAdapter
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class ManyToManyTest : BaseUnitTest() {

    @Test
    fun testCanCreateManyToMany() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val artistModel =
                artistAdapter.save(Artist(name = "Andrew Grosner"))
                    .getOrThrow()
            val songModel =
                songAdapter.save(Song(name = "Livin' on A Prayer"))
                    .getOrThrow()
            val artistSong = Artist_Song(
                0,
                artistModel,
                songModel
            )
            // TODO: generated join table
            assert(artistSongAdapter.save(artistSong).isSuccess)
        }
    }
}