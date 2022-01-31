package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.artist
import com.dbflow5.artist_Song
import com.dbflow5.config.database
import com.dbflow5.config.withTransaction
import com.dbflow5.song
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class ManyToManyTest : BaseUnitTest() {

    @Test
    fun testCanCreateManyToMany() = runBlockingTest {
        database<TestDatabase>().withTransaction {
            val artistModel =
                artist.save(Artist(name = "Andrew Grosner"))
                    .getOrThrow()
            val songModel =
                song.save(Song(name = "Livin' on A Prayer"))
                    .getOrThrow()
            val artistSong = Artist_Song(
                0,
                artistModel,
                songModel
            )
            assert(artist_Song.save(artistSong).isSuccess)
        }
    }
}