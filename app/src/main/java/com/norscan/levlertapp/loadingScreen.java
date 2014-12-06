package com.norscan.levlertapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class loadingScreen extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        ImageView image = (ImageView) findViewById(R.id.levlertLogo);
        image.setImageResource(R.drawable.levlert_logo);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(loadingScreen.this,LoginActivity.class);
                loadingScreen.this.startActivity(mainIntent);
                loadingScreen.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
