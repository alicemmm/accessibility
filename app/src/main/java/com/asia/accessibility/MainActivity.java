package com.asia.accessibility;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.main_install);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInstall();
            }
        });
    }

    private void startInstall() {
        boolean isOn = AppInstaller.isAccessibilitySettingsOn(MainActivity.this);
        if (isOn) {
            Toast.makeText(MainActivity.this, "已开启", Toast.LENGTH_SHORT).show();
        } else {
            AppInstaller.toAccessibilityService(MainActivity.this);
        }
    }

}
