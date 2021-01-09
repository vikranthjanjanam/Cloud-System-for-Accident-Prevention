package com.iot.project.gonggo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button startStop;
    private TextView gpsText;
    private BroadcastReceiver broadcastReceiver;
    private boolean requestUpdates = false;
    private String keys[], datamain[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!SharedPrefManager.getInstance(this).isLoggedin()){
            Intent login = new Intent(MainActivity.this, SigninActivity.class);
            startActivity(login);
            finish();
        }
        keys = new String[]{"Vehicle", "Latitude", "Longitude", "Speed", "Bearing", "Address", "Date", "Time", "Status"};

        gpsText = findViewById(R.id.gpsData);

        Intent current = getIntent();
        if(!current.hasExtra("FromStart")){
            Bundle xtr = getIntent().getExtras();
            String type = getIntent().getType();
            gpsText.setText("Type : " + type);
            for(String key : xtr.keySet()){
                gpsText.append("\n" + key + " : " + xtr.getString(key));
            }
        }/*else if(current.hasExtra("Main")) {
            Bundle responseData = current.getExtras();
            datamain = (String[]) responseData.get("Main");
            if(current.hasExtra("error")){
                gpsText.setText((String)responseData.get("error"));
            }else if(current.hasExtra("response")){
                gpsText.setText((String)responseData.get("response"));
            }else {
                gpsText.setText("GPS Data\n");
                for (int i = 0; i < 9; i++) {
                    gpsText.append(keys[i] + " : " + datamain[i] + "\n");
                }
            }
        }*/
        startStop = findViewById(R.id.toggle);

        if(!runtimePermissions())
            enableButtons();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(intent.hasExtra("Main")) {
                        String resp = null;
                        Bundle responseData = intent.getExtras();
                        if(intent.hasExtra("error")){
                            gpsText.setText((String)responseData.get("error"));
                        }else {
                            datamain = (String[]) responseData.get("Main");
                            gpsText.setText("GPS Data\n");
                            for(int i = 0; i < 9; i++){
                                gpsText.append(keys[i] + " : " + datamain[i] + "\n");
                            }
                        }
                    }
                }
            };
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100) {
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                enableButtons();
            }
            else {
                runtimePermissions();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    private boolean runtimePermissions() {
        if(Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        else if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Runtime request permission
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 100);
            return true;
        }
        return false;
    }

    private void enableButtons() {
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (requestUpdates) {
                    startStop.setText("Start");
                    requestUpdates = false;
                    Intent intent = new Intent(getApplicationContext(), GPS_Service.class);
                    stopService(intent);
                    Intent intnt = new Intent(getApplicationContext(), DataSender.class);
                    stopService(intnt);
                    unregisterReceiver(broadcastReceiver);
                } else {
                    startStop.setText("Stop");
                    requestUpdates = true;
                    Intent intent = new Intent(getApplicationContext(), GPS_Service.class);
                    startService(intent);
                    Intent intnt = new Intent(getApplicationContext(), DataSender.class);
                    startService(intnt);
                    registerReceiver(broadcastReceiver, new IntentFilter("response_update"));
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menuLogout:
                SharedPrefManager.getInstance(this).logout();
                Intent login = new Intent(MainActivity.this, SigninActivity.class);
                startActivity(login);
                finish();
                break;
            case R.id.menuNotify:
                break;
        }
        return true;
    }
}
