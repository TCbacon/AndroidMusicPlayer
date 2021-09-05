package com.tcbacon.localmusicplayer;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;


@Dao
public interface SongDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Song song);

    @Update
    void update(Song song);

    @Delete
    void delete(Song song);

    @Query("DELETE FROM song_table")
    void deleteAllSongs();

    //live data used to automatically update list when there are changes
    @Query("SELECT * FROM song_table ORDER BY name ASC")
    List<Song> getAllSongs();

/*
    @Query("SELECT * FROM song_table WHERE _id IN(:songIds)")
    public abstract List findSongByIds(int[] songIds);
*/

}
