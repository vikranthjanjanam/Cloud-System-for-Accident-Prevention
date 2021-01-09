package com.iot.project.gonggosimulator;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Button startStop;
    private TextView gpsText;
    private BroadcastReceiver broadcastReceiver;
    private boolean requestUpdates = false;
    private String keys[], datamain[], sendurl;
    private double timestamp;
    private Timer timer;
    private MyTimerTask myTimerTask;
    private RequestQueue requestQueue;
    private String vh;

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
        datamain = new String[9];

        gpsText = findViewById(R.id.gpsData);

        Intent current = getIntent();
        if(!current.hasExtra("FromStart")){
            Bundle xtr = getIntent().getExtras();
            String type = getIntent().getType();
            gpsText.setText("Type : " + type);
            for(String key : xtr.keySet()){
                gpsText.append("\n" + key + " : " + xtr.getString(key));
            }
        }
        if(current.hasExtra("Timestamp")){
            timestamp = Double.parseDouble((String) current.getStringExtra("Timestamp"));
        }
        startStop = findViewById(R.id.toggle);

        sendurl = Constants.StaticIP + "demo.php";
        vh = SharedPrefManager.getInstance(this).getKeyUserVehicle();
        initQueue();

        timer = new Timer();
        myTimerTask = new MyTimerTask();

        if(!runtimePermissions()) {
            enableButtons();
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
                    timer.cancel();
                } else {
                    startStop.setText("Stop");
                    requestUpdates = true;
                    timer.schedule(myTimerTask, 1000, 1000);
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

    private class MyTimerTask extends TimerTask{

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendData();
                    timestamp++;
                }
            });
        }
    }

    private void initQueue(){
        Cache cache = new DiskBasedCache(getCacheDir(), 1024*1024);
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();
    }

    private void sendData() {
        StringRequest request = new StringRequest(Request.Method.POST,
                sendurl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                gpsText.setText(response);
                /*try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(!jsonObject.getBoolean("error")){
                        int i = 0;
                        gpsText.setText("GPS Data:\n");
                        for(i = 0; i < 9; i++){
                            datamain[i] = jsonObject.getString(keys[i]);
                            gpsText.append(keys[i] + " : " + datamain[i] + "\n");
                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }*/
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                gpsText.setText(error.getCause().toString());
            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Vehicle", vh);
                params.put("Timestamp", String.valueOf(timestamp));
                return params;
            }
        };
        requestQueue.add(request);
    }
}
