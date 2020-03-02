package com.teskalabs.seacat.demojava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.teskalabs.seacat.SeaCat;

public class SplashActivity extends AppCompatActivity {

    Handler handler;
    Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (SeaCat.getInstance().getReady()) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                } else {
                    handler.postDelayed(timerRunnable, 500);
                }

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(timerRunnable);
    }

    @Override
    public void onBackPressed() {
        // Disable back button
    }

}
