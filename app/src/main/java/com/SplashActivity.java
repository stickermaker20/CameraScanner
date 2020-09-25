package com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cam.pdf.and.doc.india.scanner.R;
import com.cam.pdf.and.doc.india.scanner.camscanner.MainActivity;

public class SplashActivity extends AppCompatActivity {
    int progress = 0;
    ProgressBar simpleProgressBar;
    TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        name = findViewById(R.id.name_start);
        name.setText(Html.fromHtml("<font color=\"#FF9800\">" + "CAM" + "</font>"));
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 3500);
    }
}