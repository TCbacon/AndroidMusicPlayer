package com.tcbacon.localmusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import java.util.concurrent.Executors;

public class EditSongInfoActivity extends AppCompatActivity {

    public final static String SONG_KEY = "song";
    private Song song;
    private EditText editTxtTitle,  editTxtArtist, editTxtAlbum, editTxtYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_song_info);

        SongDatabase db = SongDatabase.getInstance(this);

         editTxtTitle = findViewById(R.id.editTxtTitle);
         editTxtArtist = findViewById(R.id.editTxtArtist);
         editTxtAlbum = findViewById(R.id.editTxtAlbum);
         editTxtYear = findViewById(R.id.editTxtYear);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if(bundle != null) {
            song = bundle.getParcelable(SONG_KEY);
            if(song != null) {
                editTxtTitle.setText(song.getName());
                editTxtArtist.setText(song.getArtist());
                editTxtAlbum.setText(song.getAlbum());
                editTxtYear.setText(String.valueOf(song.getYear()));
            }
        }


        saveInfoHandler(db);

    }

    private void saveInfoHandler(SongDatabase db) {
        Button btnSave = findViewById(R.id.btnSaveEditInfo);

        btnSave.setOnClickListener(view -> {
            if (!isEmpty()) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    if (song != null) {
                        song.setName(editTxtTitle.getText().toString().trim());
                        song.setArtist(editTxtArtist.getText().toString().trim());
                        song.setAlbum(editTxtAlbum.getText().toString().trim());
                        try{
                            song.setYear(Integer.parseInt(editTxtYear.getText().toString().trim()));
                        }

                        catch (NumberFormatException e){
                            song.setYear(0);
                        }

                        db.songDAO().update(song);

                        runOnUiThread(this::onBackPressed);
                    }
                });
            }
        });
    }

    /**
     * check if any of the fields are empty
     * @return boolean
     */
    private boolean isEmpty() {
        if (editTxtTitle.getText().toString().trim().length() == 0) {
            editTxtTitle.setError("Cannot be empty");
        }

        if (editTxtArtist.getText().toString().trim().length() == 0) {
            editTxtArtist.setError("Cannot be empty");
        }

        if (editTxtAlbum.getText().toString().trim().length() == 0) {
            editTxtAlbum.setError("Cannot be empty");
        }

        if (editTxtYear.getText().toString().trim().length() == 0){
            editTxtYear.setError("Cannot be empty");
        }
        return editTxtTitle.getText().toString().trim().length() == 0
                || editTxtTitle.getText().toString().trim().length() == 0
                || editTxtAlbum.getText().toString().trim().length() == 0
    || editTxtYear.getText().toString().trim().length() == 0;
    }

}

