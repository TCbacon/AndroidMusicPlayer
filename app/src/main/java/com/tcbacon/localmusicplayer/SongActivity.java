package com.tcbacon.localmusicplayer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SongActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    private TextView txtViewSongTitle;
    private TextView txtViewSongDuration;
    private ImageView imgViewSong;
    private ProgressBar progressBarLoadingImg;

    private ImageButton btnPlayAndPause;

    private Uri songUri;
    private String customImgUriString ="";
    private String uriString = "";

    private SeekBar seekBarSong;
    private Runnable runnable;
    private Handler handler;
    private boolean isPaused = true;
    private boolean isSongPlayingScreenRotation = false;

    private int songLength = 0;
    public static List<Song> songs;
    private int currentSongIndex = 0;
    private int songLengthFetchRotation = 0;
    private int seekBarSetMaxForRotation = 0;


    public static final String SETLIST_POS = "SETLIST_POS";
    private boolean isAutoPlay = false;
    private boolean isAudioBarsChecked = false;
    private boolean isAutoPause = false;

    private BarVisualizer barVisualizer;
    private ActivityResultLauncher<Intent> mLauncher;

    private Button btnRotateImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        sharedPrefsHandler();

        mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getData() != null) {

                            Uri customImgUri = result.getData().getData();
                            SongDatabase db = SongDatabase.getInstance(SongActivity.this);

                            Executors.newSingleThreadExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    if (customImgUri != null) {
                                        // to keep the URIs valid over restarts need to persist access permission

                                        final int takeFlags = result.getData().getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                        ContentResolver resolver = getContentResolver();
                                        resolver.takePersistableUriPermission(customImgUri, takeFlags);

                                        //we assign uri to string to change the image dynamically
                                        customImgUriString = customImgUri.toString();
                                        Song song = songs.get(currentSongIndex);

                                        try{
                                            //revoke permissions if custome uri is not empty and the new uri does not equal the current one
                                            if(!song.getCustomImageUri().equals("") && !song.getCustomImageUri().equals(customImgUriString)){
                                                resolver.releasePersistableUriPermission(Uri.parse(song.getCustomImageUri()), takeFlags);
                                            }
                                        }

                                        catch (SecurityException e){
                                           //pass
                                        }

                                        song.setCustomImageUri(customImgUriString);
                                        db.songDAO().update(song);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                setSongImg();
                                            }
                                        });

                                    }
                                }

                            });
                        }
                    }
                });


        initViews();
        initSeekBarRunnable();
        initSeekBarListener();
        setSongInfo(savedInstanceState);
        displayAutoPlayMsg();
    }


    /**
     * Retrieve saved info or setting from settings activity
     */
    private void sharedPrefsHandler() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isAutoPlay = prefs.getBoolean(SettingsActivity.AUTO_PLAY_PREF, false);
        isAudioBarsChecked = prefs.getBoolean(SettingsActivity.BAR_VISUALIZER_PREF, false);
        isAutoPause = prefs.getBoolean(SettingsActivity.AUTO_PAUSE_PREF, false);
    }

    /**
     * Display auto play msg if enabled
     */
    private void displayAutoPlayMsg() {
        TextView txtViewAutoPlay = findViewById(R.id.txtViewAutoPlayMsg);

        if(isAutoPlay){
            txtViewAutoPlay.setVisibility(View.VISIBLE);
        }

        else{
            txtViewAutoPlay.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onPause() {
        if(isAutoPause) {
            releaseMediaPlayer();
            btnPlayAndPause.setImageResource(R.drawable.ic_play_song);
        }

        super.onPause();
    }

    /**
     * if user goes back to app stay paused and let user resume music
     */
    @Override
    protected void onResume() {
        if(isAutoPause){
            txtViewSongDuration.setText(songDurationHandler(songLength));
            seekBarSong.setMax(seekBarSetMaxForRotation);
            seekBarSong.setProgress(songLength/1000);
        }
        super.onResume();
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState.getBoolean("isSongPlaying")) {
            btnPlayAndPause.setImageResource(R.drawable.ic_pause_black_24dp);
            playSong(savedInstanceState.getInt("songLength"));
        }

        if(savedInstanceState.getBoolean("isSongPaused")){
            btnPlayAndPause.setImageResource(R.drawable.ic_play_song);
        }

        songLength = savedInstanceState.getInt("songLength");
        seekBarSetMaxForRotation = savedInstanceState.getInt("seekBarCurrentSongMax");

        //prevents duration from resetting to 0 when user rotates multiple times
        songLengthFetchRotation = songLength;
        isSongPlayingScreenRotation = savedInstanceState.getBoolean("isSongPlaying");
        isPaused = savedInstanceState.getBoolean("isSongPaused");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("currentSongIndex", currentSongIndex);
        outState.putInt("songLength", songLengthFetchRotation);
        outState.putBoolean("isSongPaused", isPaused);
        outState.putBoolean("isSongPlaying", isSongPlayingScreenRotation);
        outState.putInt("seekBarCurrentSongMax", seekBarSetMaxForRotation);
    }


    /**
     * To change image of song to a custom image
     * @param v button view
     */
    public void openCustomImageFile(View v){

        //REQUEST PERMISSIONS WHEN PICKING A CUSTOM IMAGE
        Intent intent;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }
        else{
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        mLauncher.launch(intent);

    }

    private void setSongInfo(Bundle saveInstance) {
        Intent intent = getIntent();

        int startSongPos;
        if(saveInstance != null){
            startSongPos = saveInstance.getInt("currentSongIndex");

        }
        else {
            startSongPos = intent.getIntExtra(SETLIST_POS, -1);
        }

        if (startSongPos > -1 && !songs.isEmpty()) {
            String songTitle = songs.get(startSongPos).getName();
            songUri = Uri.parse(songs.get(startSongPos).getDefaultSongUri());
            uriString = songUri.toString();
            customImgUriString = songs.get(startSongPos).getCustomImageUri();
            txtViewSongTitle.setText(songTitle);
            currentSongIndex = startSongPos;


            Button btnNextSong = findViewById(R.id.btnNextSong);
            btnNextSong.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    nextPrevPlayerReset();
                    btnPlayAndPause.setImageResource(R.drawable.ic_play_song);

                    if (currentSongIndex < songs.size() - 1) {
                        currentSongIndex += 1;
                        changeSongHandler();
                    } else {
                        currentSongIndex = 0;
                        changeSongHandler();
                    }
                }
            });

            Button btnPrev = findViewById(R.id.btnPrev);
            btnPrev.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    nextPrevPlayerReset();
                    btnPlayAndPause.setImageResource(R.drawable.ic_play_song);

                    if (currentSongIndex > 0) {
                        currentSongIndex -= 1;
                        changeSongHandler();
                    } else {
                        currentSongIndex = songs.size()-1;
                        changeSongHandler();
                    }
                }
            });

            setSongImg();
        }

        else{
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Reset media player for when pressing on next or previous song button
     */
    private void nextPrevPlayerReset() {
        releaseMediaPlayer();
        songLength = 0;
        songLengthFetchRotation = 0;
        seekBarSong.setProgress(songLength);
        txtViewSongDuration.setText(songDurationHandler(songLength));
        isSongPlayingScreenRotation = false;

    }

    /**
     * Handles changing song info when pressing on  next or previous song buttons
     */
    private void changeSongHandler() {
        txtViewSongTitle.setText(songs.get(currentSongIndex).getName());
        songUri = Uri.parse(songs.get(currentSongIndex).getDefaultSongUri());
        customImgUriString = songs.get(currentSongIndex).getCustomImageUri();
        setSongImg();
    }

    private void setSongImg() {

        progressBarLoadingImg.setVisibility(View.VISIBLE);

        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();

            if (!customImgUriString.equals("")) {
                btnRotateImg.setVisibility(View.VISIBLE);
                Picasso.get().load(Uri.parse(customImgUriString)).fit().centerCrop().rotate(songs.get(currentSongIndex).getRotation()).into(imgViewSong, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBarLoadingImg.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        btnRotateImg.setVisibility(View.GONE);
                        progressBarLoadingImg.setVisibility(View.GONE);
                        imgViewSong.setImageResource(R.drawable.lmp_missing_image);
                    }
                });
            } else {

                btnRotateImg.setVisibility(View.GONE);
                progressBarLoadingImg.setVisibility(View.GONE);
                mmr.setDataSource(this, songUri);

                byte[] data = mmr.getEmbeddedPicture();

                // convert the byte array to a bitmap
                if (data != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    imgViewSong.setImageBitmap(bitmap);
                } else {
                    imgViewSong.setImageResource(R.drawable.lmp_missing_image);
                }
            }


        } catch (Exception e) {
            imgViewSong.setImageResource(R.drawable.lmp_missing_image);
            btnRotateImg.setVisibility(View.GONE);
        }

    }


    private void initSeekBarListener() {
        seekBarSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(mediaPlayer != null && b) {

                    mediaPlayer.seekTo(i*1000);
                }
                else if(b){
                    songLength = i * 1000;
                    txtViewSongDuration.setText(songDurationHandler(songLength));
                    songLengthFetchRotation = songLength;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initSeekBarRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {
                updateSongSeekBar();
            }
        };
    }

    private void initViews() {
        txtViewSongTitle = findViewById(R.id.txtViewSongTitle);
        txtViewSongTitle.setSelected(true);
        txtViewSongDuration = findViewById(R.id.txtViewSongDuration);
        btnPlayAndPause = findViewById(R.id.btnPlayPause);
        btnRotateImg = findViewById(R.id.btnRotateImg);
        seekBarSong = findViewById(R.id.seekBarSong);
        imgViewSong = findViewById(R.id.imageViewSong);
        progressBarLoadingImg = findViewById(R.id.progressBarLoadingImg);
        handler = new Handler(Looper.getMainLooper());

        if(isAudioBarsChecked) {
            barVisualizer = findViewById(R.id.barVisualizer);
            barVisualizer.setVisibility(View.VISIBLE);
        }

        else{
            if(barVisualizer != null) {
                barVisualizer.setVisibility(View.GONE);
            }
        }
        playResumePauseOnClickHandler();
        rotateSongImage();
    }

    /**
     *  Rotate song image and save changes
     */
    private void rotateSongImage() {
        btnRotateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SongDatabase db = SongDatabase.getInstance(SongActivity.this);
                songs.get(currentSongIndex).setRotation((songs.get(currentSongIndex).getRotation() + 90) % 360);

                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        db.songDAO().update(songs.get(currentSongIndex));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setSongImg();
                            }
                        });
                    }


                });
            }
        });
    }

    private void playResumePauseOnClickHandler() {
        btnPlayAndPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPaused) {
                    btnPlayAndPause.setImageResource(R.drawable.ic_pause_black_24dp);
                    playSong(songLength);
                }

                else{
                    releaseMediaPlayer();
                    btnPlayAndPause.setImageResource(R.drawable.ic_play_song);
                }

            }
        });
    }

    public void onClickBtnBackSong(View v){
        releaseMediaPlayer();
        onBackPressed();
    }

    public void playSong(final int length){

        isPaused = false;
        isSongPlayingScreenRotation = true;

        //Check if uri exists
        if(!uriString.equals("")) {
            mediaPlayer = new MediaPlayer();
        }

        if(mediaPlayer != null) {
            try {

                mediaPlayer.reset();
                mediaPlayer.setDataSource(getApplicationContext(), songUri);

            } catch (IllegalArgumentException e) {
                mediaErrorMessage("1");
            } catch (SecurityException e) {
                mediaErrorMessage("2");
            } catch (IllegalStateException e) {
                mediaErrorMessage("3");
            } catch (IOException e) {
                mediaErrorMessage("4");
            }

            try {
                if(mediaPlayer != null) {
                    mediaPlayer.prepareAsync();
                }
            } catch (IllegalStateException e) {
                mediaErrorMessage("5");
            } catch (SecurityException e) {
                mediaErrorMessage("6");
            }

            //check to see if medial player is null if exception is thrown above
            if (mediaPlayer != null) {
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                        seekBarSetMaxForRotation = mediaPlayer.getDuration() / 1000;
                        seekBarSong.setMax(mediaPlayer.getDuration() / 1000);
                        mediaPlayer.seekTo(length);

                        if (isAudioBarsChecked) {
                            int audioSessioId = mediaPlayer.getAudioSessionId();
                            if (audioSessioId != -1) {
                                barVisualizer.setAudioSessionId(audioSessioId);
                            }
                        }

                        if (!isPaused) {

                            updateSongSeekBar();
                            afterMusicFinishHandler();
                        }
                    }
                });
            }
        }
    }

    /**
     * Release the music player if error occurs when playing it
     */
    private void mediaErrorMessage(String type){
        Toast.makeText(getApplicationContext(), "Error occurred with current song...", Toast.LENGTH_SHORT).show();
        releaseMediaPlayer();
        btnPlayAndPause.setImageResource(R.drawable.ic_play_song);
    }


    private void afterMusicFinishHandler() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                if(isAutoPlay) {
                    releaseMediaPlayer();
                    songLength = 0;
                    songLengthFetchRotation = 0;
                    seekBarSong.setProgress(songLength);
                    txtViewSongDuration.setText(songDurationHandler(songLength));
                    isSongPlayingScreenRotation = false;
                    btnPlayAndPause.setImageResource(R.drawable.ic_pause_black_24dp);

                    if (currentSongIndex < songs.size() - 1) {
                        currentSongIndex += 1;
                        changeSongHandlerAutoPlay();
                    } else {
                        currentSongIndex = 0;
                        changeSongHandlerAutoPlay();
                    }

                    playSong(songLength);
                }

                else {
                    releaseMediaPlayer();
                    songLength = 0;
                    btnPlayAndPause.setImageResource(R.drawable.ic_play_song);
                }
            }
        });
    }

    private void changeSongHandlerAutoPlay() {
        txtViewSongTitle.setText(songs.get(currentSongIndex).getName());
        songUri = Uri.parse(songs.get(currentSongIndex).getDefaultSongUri());
        customImgUriString = songs.get(currentSongIndex).getCustomImageUri();
        setSongImg();
    }

    private void releaseMediaPlayer() {
        isSongPlayingScreenRotation = false;
        if (mediaPlayer != null) {
            //get current pos of song
            songLength = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            isPaused = true;
        }

        if(barVisualizer != null){
            barVisualizer.release();
        }
    }


    private String songDurationHandler(int songDurInMili){
        //convert the song duration into string reading hours, mins seconds
        int hrs = (songDurInMili / 3600000);
        int mns = (songDurInMili / 60000) % 60000;
        int scs = songDurInMili % 60000 / 1000;

        return String.format(Locale.ENGLISH,"%02d:%02d:%02d", hrs,  mns, scs);
    }


    private void updateSongSeekBar(){
        if(mediaPlayer != null && seekBarSong != null){

            try {
                seekBarSong.setProgress(mediaPlayer.getCurrentPosition() /1000);
                songLengthFetchRotation = mediaPlayer.getCurrentPosition();
                txtViewSongDuration.setText(songDurationHandler(mediaPlayer.getCurrentPosition()));
                handler.postDelayed(runnable, 1000);
            }

            catch (Exception e){
                //pass
            }
        }
    }



    /**
     * When user closes the app release music player
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.release();
        }

        if(barVisualizer != null){
            barVisualizer.release();
        }

        if(btnRotateImg != null){
            btnRotateImg.setOnClickListener(null);
        }

        if(btnPlayAndPause != null){
            btnPlayAndPause.setOnClickListener(null);
        }
    }
}
