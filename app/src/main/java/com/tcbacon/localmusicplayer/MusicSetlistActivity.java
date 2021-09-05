package com.tcbacon.localmusicplayer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Executors;


public class MusicSetlistActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 1;
    private ArrayList<Song> songs = new ArrayList<>();
    private ArrayList<Song> favoriteSongs = new ArrayList<>();
    private SongSetlistAdapter adapter;
    private int MAX_SETLIST_LENGTH = 50000;

    private ProgressBar  progressBarLoadingSetlist;
    private TextView txtViewEmptySongList;

    private String name = "Unavailable";
    private String artist = "Unavailable";
    private String album = "Unavailable";
    private String year = "0";
    private ActivityResultLauncher<Intent> mLauncher;
    public static int sortTrackerNumber = -1;

    private Spinner dropdown;

    //interface used to pause and resume in adapter
    private AdapterLifeCycleState adapterLifeCycleState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_setlist);


        progressBarLoadingSetlist = findViewById(R.id.progressBarLoadingSetlist);
        txtViewEmptySongList = findViewById(R.id.txtViewEmptyList);


        //populate list after user selects a song
        initActionPickAudioResult();
        initSongSetlistAdapter();
        initButtonsOnClick();
        loadSongsFromDBToList();
        initDropDownMenu();
        initAd();
    }

    /**
     * Initialize ad
     */
    private void initAd(){
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }
        });
    }

    private void initButtonsOnClick() {
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MusicSetlistActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });


        Button btnAddSong = findViewById(R.id.btnAddSong);
        btnAddSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAudioFile();
            }
        });
    }

    private void initDropDownMenu() {
        dropdown = findViewById(R.id.spinnerDropDownSongSort);
        String[] items = new String[]{"Song", "Artist", "Album", "Year", "Favorites"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                SongDatabase db = SongDatabase.getInstance(MusicSetlistActivity.this);

                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {

                        songs = (ArrayList<Song>) db.songDAO().getAllSongs();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (i) {
                                    case 0:
                                        sortTrackerNumber = 0;
                                        Collections.sort(songs, new Comparator<Song>() {
                                            @Override
                                            public int compare(Song song, Song t1) {
                                                return song.getName().compareToIgnoreCase(t1.getName());
                                            }
                                        });
                                        loadSongsAfterSorting();
                                        break;
                                    case 1:
                                        sortTrackerNumber = 1;
                                        Collections.sort(songs, new Comparator<Song>() {
                                            @Override
                                            public int compare(Song song, Song t1) {
                                                return song.getArtist().compareToIgnoreCase(t1.getArtist());

                                            }
                                        });
                                        loadSongsAfterSorting();
                                        break;
                                    case 2:
                                        sortTrackerNumber = 2;
                                        Collections.sort(songs, new Comparator<Song>() {
                                            @Override
                                            public int compare(Song song, Song t1) {
                                                return song.getAlbum().compareToIgnoreCase(t1.getAlbum());
                                            }
                                        });
                                        loadSongsAfterSorting();
                                        break;
                                    case 3:
                                        sortTrackerNumber = 3;
                                        Collections.sort(songs, new Comparator<Song>() {
                                            @Override
                                            public int compare(Song song, Song t1) {

                                                return Integer.compare(song.getYear(), t1.getYear());
                                            }
                                        });
                                        loadSongsAfterSorting();
                                        break;
                                    case 4:
                                        sortTrackerNumber = 4;
                                        loadFavoriteSongs();
                                        break;
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadSongsFromDBToList();
        if(dropdown != null){
            dropdown.setSelection(0);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean(SettingsActivity.BAR_VISUALIZER_PREF, false).apply();
        }
    }


    private void initActionPickAudioResult() {
        mLauncher = registerForActivityResult(
              new ActivityResultContracts.StartActivityForResult(),
              new ActivityResultCallback<ActivityResult>() {
                  @Override
                  public void onActivityResult(ActivityResult result) {

                      if(result.getData() == null){
                          return;
                      }

                      Uri uri = result.getData().getData();

                      if(uri != null) {
                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                              name = setSongName(MusicSetlistActivity.this.getContentResolver(), uri);
                              setNewSongInfoAPI30(MusicSetlistActivity.this.getContentResolver(), uri);
                          }

                          else {
                              name = setSongName(MusicSetlistActivity.this.getContentResolver(), uri);
                              setSongInfoLessThanAPI30(uri);
                          }

                          SongDatabase db = SongDatabase.getInstance(MusicSetlistActivity.this);
                          Executors.newSingleThreadExecutor().execute(new Runnable() {
                              @Override
                              public void run() {

                                  if (songs.size() < MAX_SETLIST_LENGTH) {
                                      Song song = new Song();
                                      song.setName(name);
                                      song.setAlbum(album);
                                      song.setArtist(artist);
                                      song.setDefaultSongUri(uri.toString());
                                      try {
                                          song.setYear(Integer.parseInt(year));
                                      } catch (NumberFormatException e) {
                                          song.setYear(0);
                                      }
                                      db.songDAO().insert(song);

                                      loadSongsFromDBToList();
                                  } else {
                                      runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              Toast.makeText(MusicSetlistActivity.this, "SETLIST LIMIT REACHED...", Toast.LENGTH_SHORT).show();
                                          }
                                      });

                                  }
                              }

                          });
                      }

                      else{
                          Toast.makeText(MusicSetlistActivity.this, "Something went wrong retrieving music...",Toast.LENGTH_SHORT).show();
                      }
                  }
              });

    }

    private void loadFavoriteSongs() {
        favoriteSongs.clear();

        for (Song s : songs) {
            if (s.getIsFavorite() == 1) {
                favoriteSongs.add(s);
            }
        }
        adapter.setSongListFull(favoriteSongs);
    }


    private void loadSongsAfterSorting(){
        adapter.setSongListFull(songs);
    }


    private void loadSongsFromDBToList() {
        SongDatabase db = SongDatabase.getInstance(MusicSetlistActivity.this);

        Executors.newSingleThreadExecutor().execute(new Runnable() {

            @Override
            public void run() {
                songs = (ArrayList<Song>) db.songDAO().getAllSongs();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setSongListFull(songs);
                        progressBarLoadingSetlist.setVisibility(View.GONE);
                        if(songs.size() > 0) { txtViewEmptySongList.setVisibility(View.GONE); }
                        else {
                            txtViewEmptySongList.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }


    /**
     * Set the song name in the setlist
     * @param resolver a client to ContentProvider, the resolver provides CRUD operations
     * @param uri the path of the song
     * @return String name
     */
    private String setSongName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }


    /**
     * For API levels 29 and below
     * @param uri song uri
     */

    public void setSongInfoLessThanAPI30(Uri uri) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, uri);

        artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        year = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);

        if(artist == null){
            artist = "Unavailable";
        }

        if(album == null){
            album = "Unavailable";
        }

        if(year == null){
            year = "0";
        }
    }


    @RequiresApi(api = 30)
    private void setNewSongInfoAPI30(ContentResolver resolver, Uri uri) {

        boolean isQuery = false;

        String[] cursor_cols = {MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR};

        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final Cursor cursor = resolver.query(uri,
                cursor_cols, where, null, null);

        if (cursor != null) {

            while (cursor.moveToNext()) {
                isQuery = true;
                artist = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                album = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                year = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));
            }

            if (!isQuery) {
                artist = "Unavailable";
                album = "Unavailable";
                year = "0";
            }

            cursor.close();
        }
    }


    private void openAudioFile(){
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            } else {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                mLauncher.launch(galleryIntent);
            }
        } catch (Exception e) {
            //pass
        }
    }


    /**
     * Initialize song setlist adapter
     */
    private void initSongSetlistAdapter(){
        SongDatabase db = SongDatabase.getInstance(MusicSetlistActivity.this);
        RecyclerView setlistRecyclingView = findViewById(R.id.setlistRecyclingView);
        adapter = new SongSetlistAdapter(db,this);
        adapterLifeCycleState =  adapter.registerActivityState();

         setlistRecyclingView.setAdapter(adapter);
        setlistRecyclingView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                mLauncher.launch(galleryIntent);
            } else {
                // User refused to grant permission.
                Toast.makeText(MusicSetlistActivity.this, "Click on gear icon to give permission manually",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(adapterLifeCycleState != null){
            adapterLifeCycleState.onPaused();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.search_song, menu);
        MenuItem searchItem = menu.findItem(R.id.actionSearch);

        if(searchItem != null) {
            SearchView searchViewSongName = (SearchView) searchItem.getActionView();
            searchViewSongName.setImeOptions(EditorInfo.IME_ACTION_DONE);
            searchViewSongName.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    adapter.getFilter().filter(s);
                    return true;
                }
            });

            return true;
        }

        return false;
    }
}
