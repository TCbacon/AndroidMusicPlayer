package com.tcbacon.localmusicplayer;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Objects;

@Entity(tableName = "song_table",indices = @Index(value = {"default_song_uri"}, unique = true))
public class Song implements Parcelable {

    //with each row added increment id auto = true
    //uniquely identify entry
    @PrimaryKey(autoGenerate = true)
    private int _id;

    @ColumnInfo(name = "name")
    private String name = "Unavailable";

    //change uri to a string
    @ColumnInfo(name = "default_song_uri")
    private String defaultSongUri;

    @ColumnInfo(name ="custom_image_uri")
    private String customImageUri = "";

    @ColumnInfo(name = "album")
    private String album = "Unavailable";

    @ColumnInfo(name="artist")
    private String artist = "Unavailable";

    @ColumnInfo(name="year")
    private int year = 0;

    @ColumnInfo(name="isFavorite")
    private int isFavorite = 0;

    @ColumnInfo(name="imgRotation")
    private float rotation = 0;


    public Song(){}

    @Ignore
    public Song(String name, String defaultSongUri, String album, String artist, int year, int isFavorite) {
        this.name = name;
        this.defaultSongUri = defaultSongUri;
        this.album = album;
        this.artist = artist;
        this.year = year;
        this.isFavorite = isFavorite;
    }

    protected Song(Parcel in) {
        _id = in.readInt();
        name = in.readString();
        defaultSongUri = in.readString();
        customImageUri = in.readString();
        album = in.readString();
        artist = in.readString();
        year = in.readInt();
        isFavorite = in.readInt();
        rotation = in.readFloat();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(int isFavorite) {
        this.isFavorite = isFavorite;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }


    public String getCustomImageUri() {
        return customImageUri;
    }

    public void setCustomImageUri(String customImageUri) {
        this.customImageUri = customImageUri;
    }

    public String getDefaultSongUri() {
        return defaultSongUri;
    }

    public void setDefaultSongUri(String defaultSongUri) {
        this.defaultSongUri = defaultSongUri;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public float getRotation() {
        return rotation;
    }
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, defaultSongUri,customImageUri, _id, album, artist, year, isFavorite, rotation);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Song){
            Song other = (Song) obj;
            return name.equals(other.name) &&
                    defaultSongUri.equals(other.defaultSongUri) &&
                    customImageUri.equals(other.customImageUri) &&
                    album.equals(other.album) &&
                    artist.equals(other.artist) &&
                    year == other.year &&
                    isFavorite == other.isFavorite &&
                    _id == other._id &&
                    rotation == other.rotation;
        }
        return false;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(_id);
        parcel.writeString(name);
        parcel.writeString(defaultSongUri);
        parcel.writeString(customImageUri);
        parcel.writeString(album);
        parcel.writeString(artist);
        parcel.writeInt(year);
        parcel.writeInt(isFavorite);
        parcel.writeFloat(rotation);
    }
}
