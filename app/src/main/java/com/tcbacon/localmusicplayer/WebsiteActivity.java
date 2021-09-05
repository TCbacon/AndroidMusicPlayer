package com.tcbacon.localmusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebsiteActivity extends AppCompatActivity {

    private WebView webView;
    public static String PRIVACY_POLICY_URL ="privacy";
    public static String TERMS_AND_CONDITIONS_URL ="terms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_website);
        webViewHandler();
    }


    private void webViewHandler(){
        webView = findViewById(R.id.webView);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null) {
            if (extras.containsKey(PRIVACY_POLICY_URL)) {
                loadUrlHandler(intent, PRIVACY_POLICY_URL);
            }

            else if(extras.containsKey(TERMS_AND_CONDITIONS_URL)){
                loadUrlHandler(intent, TERMS_AND_CONDITIONS_URL);
            }
        }
    }

    private void loadUrlHandler(Intent intent, String key) {
        String url = intent.getStringExtra(key);
        if(url != null) {
            webView.loadUrl(url);
            webView.setWebViewClient(new WebViewClient());
        }

        else{
            Toast.makeText(this, "Error loading url",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
