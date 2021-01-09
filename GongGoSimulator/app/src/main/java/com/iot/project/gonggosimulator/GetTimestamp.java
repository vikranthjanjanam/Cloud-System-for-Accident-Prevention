package com.iot.project.gonggosimulator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class GetTimestamp extends AppCompatActivity {

    private EditText ts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_timestamp);
        Button nxt = findViewById(R.id.nxtMain);
        ts = findViewById(R.id.timestamp);
        nxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tstamp = ts.getText().toString();
                Intent intnt = new Intent(GetTimestamp.this, MainActivity.class);
                intnt.putExtra("Timestamp", tstamp);
                intnt.putExtra("FromStart", "No");
                startActivity(intnt);
            }
        });
    }
}
