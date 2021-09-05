package com.tcbacon.localmusicplayer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testButtonBool(){
        Song song = new Song();
        assertEquals(song.getCustomImageUri(), "");
        assertEquals(song.getAlbum(), "NO ALBUM");
    }

    @Test
    public void songsEqual(){
        Song song = new Song("jum", "/content", "1984", "van halen",1983, 1);
        Song song1 = new Song("jum", "/content", "1984", "van halen",1984, 1);
        assertEquals(song, song1);
    }
}