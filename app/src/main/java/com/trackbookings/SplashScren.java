package com.trackbookings;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.trackbookings.Helpers.Helpers;
import com.trackbookings.R;

public class SplashScren extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_scren);
        EdgeToEdge.enable(this);
        Helpers.setStatusBarTheme(this, R.color.blue_purple, false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Helpers.getTextFromSharedPref(SplashScren.this, "id") != null){
                    startActivity(new Intent(SplashScren.this, MainActivity.class));
                    finish();
                }else {
                    startActivity(new Intent(SplashScren.this, LoginActivity.class));
                    finish();
                }
            }
        }, 1500);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}