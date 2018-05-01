package com.android.smarthome.device;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.android.smarthome.R;

public class AutoConfigWifiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_config_wifi);

        Button configBtn = (Button) findViewById(R.id.config_start);
        configBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AutoConfigWifiActivity.this, WifiConfigActivity.class);
                startActivity(intent);
            }
        });
    }
}
