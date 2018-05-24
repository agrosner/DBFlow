package com.raizlabs.dbflow5.models

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.models.Artist
import com.raizlabs.dbflow5.models.Song
import com.raizlabs.dbflow5.structure.save
import org.junit.Assert.assertTrue
import org.junit.Test

class ManyToManyTest : BaseUnitTest() {

    @Test
    fun testCanCreateManyToMany() {
        val artist = Artist(name = "Andrew Grosner")
        val song = Song(name = "Livin' on A Prayer")

        artist.save()
        song.save()

        val artistSong = Artist_Song()
        artistSong.artist = artist
        artistSong.song = song
        assertTrue(artistSong.save())


    }
}