package com.tcbacon.localmusicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {


    private CheckBox chkBoxAutoPlay;
    private CheckBox chkBoxBarVisualizer;
    private CheckBox chkBoxAutoPause;
    private Button btnHelp;
    private int REQUEST_PERMISSION = 1;
    public static String AUTO_PLAY_PREF = "autoplay";
    public static String BAR_VISUALIZER_PREF = "bar_visualizer";
    public static String AUTO_PAUSE_PREF = "autopause";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        backButtonHandler();

        chkBoxBarVisHandler();
        chkBoxAutoPlayHandler();
        chkBoxAutoPauseHandler();

        privacyButtonHandler();
        termsAndConditionsButtonHandler();
        goToHelpPage();

    }

    private void goToHelpPage() {
        btnHelp = findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });
    }


    private void privacyButtonHandler(){
        Button btnPrivacy = findViewById(R.id.btnPrivacyPolicy);
        btnPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                externalWebUrlIntentHandler(WebsiteActivity.PRIVACY_POLICY_URL, "https://sites.google.com/view/songplayerlocalprivacyandterms/home");
            }
        });
    }
    private void termsAndConditionsButtonHandler(){
        Button btnTerms = findViewById(R.id.btnTermsAndConditions);
        btnTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                externalWebUrlIntentHandler(WebsiteActivity.TERMS_AND_CONDITIONS_URL, "https://sites.google.com/view/songplayerlocalprivacyandterms/terms-and-conditions");
            }
        });
    }

    private void externalWebUrlIntentHandler(String key, String url){
        Intent intent = new Intent(SettingsActivity.this, WebsiteActivity.class);
        intent.putExtra(key, url);
        startActivity(intent);
    }

    private void backButtonHandler() {
        Button btnBack = findViewById(R.id.btnBackSettings);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void chkBoxBarVisHandler() {
        chkBoxBarVisualizer = findViewById(R.id.chkBoxBarVisualizer);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isBarVisChecked = prefs.getBoolean(BAR_VISUALIZER_PREF, false);

        if(isBarVisChecked){
            chkBoxBarVisualizer.setChecked(true);
        }

        else{
            chkBoxBarVisualizer.setChecked(false);
        }


        chkBoxBarVisualizer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked() && b) {
                    if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION);
                    }

                    else{
                        prefs.edit().putBoolean(BAR_VISUALIZER_PREF, true).apply();
                    }
                }

                else {
                    prefs.edit().putBoolean(BAR_VISUALIZER_PREF, false).apply();
                }
            }
        });
    }

    private void chkBoxAutoPlayHandler() {
        chkBoxAutoPlay = findViewById(R.id.chkBoxAutoPlay);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAutoChecked = prefs.getBoolean(AUTO_PLAY_PREF, false);

        if(isAutoChecked){
            chkBoxAutoPlay.setChecked(true);
        }

        else{
            chkBoxAutoPlay.setChecked(false);
        }


        chkBoxAutoPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(compoundButton.isChecked() && b){
                    prefs.edit().putBoolean(AUTO_PLAY_PREF, true).apply();
                }

                else{
                    prefs.edit().putBoolean(AUTO_PLAY_PREF, false).apply();
                }
            }
        });
    }


    private void chkBoxAutoPauseHandler(){
        chkBoxAutoPause = findViewById(R.id.chkBoxAutoPause);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAutoChecked = prefs.getBoolean(AUTO_PAUSE_PREF, false);

        if(isAutoChecked){
            chkBoxAutoPause.setChecked(true);
        }

        else{
            chkBoxAutoPause.setChecked(false);
        }

        chkBoxAutoPause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked() && b){
                    prefs.edit().putBoolean(AUTO_PAUSE_PREF, true).apply();
                }

                else{
                    prefs.edit().putBoolean(AUTO_PAUSE_PREF, false).apply();
                }
            }
        });
    }


    public void enableStoragePermissions(View v){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                prefs.edit().putBoolean(BAR_VISUALIZER_PREF, true).apply();
            }

            else {
                // User refused to grant permission.
                chkBoxBarVisualizer.setChecked(false);
                Toast.makeText(SettingsActivity.this, "You can manually give permission by clicking the permissions button",Toast.LENGTH_SHORT).show();
                prefs.edit().putBoolean(BAR_VISUALIZER_PREF, false).apply();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(chkBoxAutoPlay != null){
            chkBoxAutoPlay.setOnCheckedChangeListener(null);
        }

        if(chkBoxBarVisualizer != null){
            chkBoxBarVisualizer.setOnCheckedChangeListener(null);
        }

        if(btnHelp != null){
            btnHelp.setOnClickListener(null);
        }
    }
}
