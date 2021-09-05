package com.tcbacon.localmusicplayer;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {Song.class}, version = 1, exportSchema = false)
public abstract class SongDatabase extends RoomDatabase {

    private static SongDatabase instance;
    public abstract SongDAO songDAO();


    //sync so only one thread can access each time
    static synchronized SongDatabase getInstance(Context context) {
        if (instance == null) {

            //fallback is for when we increase version number to prevent illegalstateexpt
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    SongDatabase.class, "song_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}

