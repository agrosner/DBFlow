package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.kotlinextensions.save
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