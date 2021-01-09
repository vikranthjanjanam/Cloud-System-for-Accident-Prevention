package com.iot.project.gonggo;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

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
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DataSender extends Service {

    private String datamain[], keys[], sendurl;
    private Geocoder geocoder;
    private RequestQueue requestQueue;
    private int status;
    private float prevspeed;

    public DataSender() {
    }

    @Override
    public void onCreate() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        sendurl = Constants.PublicIP + "dataset.php";
        initQueue();
        keys = new String[]{"Vehicle", "Latitude", "Longitude", "Speed", "Bearing", "Address", "Date", "Time", "Status"};
        datamain = new String[9];
        geocoder = new Geocoder(this, Locale.getDefault());
        datamain[0] = SharedPrefManager.getInstance(this).getKeyUserVehicle();
        status = 0;
        registerReceiver(broadcastReceiver, new IntentFilter("location_Update"));
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("gpsData")) {
                @SuppressLint({"NewApi", "LocalSuppress"})
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time[] = sdf.format(System.currentTimeMillis()).split(" ");
                Bundle gpsData = intent.getExtras();
                Double latitude = (Double) gpsData.getDouble("Latitude");
                Double longitude = (Double) gpsData.getDouble("Longitude");
                datamain[1] = latitude.toString();
                datamain[2] = longitude.toString();
                float speed = (float) gpsData.getFloat("Speed");
                speed *= 3.6;
                if (status == 0) {
                    datamain[8] = "61713";
                    status = 1;
                } else if (speed < 10 && prevspeed < 10 && prevspeed < speed) {
                    datamain[8] = "61713";
                } else if (speed == 0) {
                    datamain[8] = "61715";
                } else {
                    datamain[8] = "61714";
                }
                prevspeed = speed;
                datamain[3] = String.format("%.2f", speed);
                datamain[4] = String.format("%f", (float) gpsData.getFloat("Bearing"));
                datamain[5] = findLocation(latitude, longitude);//gpsData.getString("Address");
                datamain[6] = time[0];
                datamain[7] = time[1];
                sendData();
            }
        }
    };

    private String findLocation(Double latitude, Double longitude) {
        List<Address> address = null;
        String err = "", addr[];
        try {
            address = geocoder.getFromLocation(latitude, longitude, 2);
        } catch (IOException e) {
            err = "True";
        }
        if(!(err.equals("True")) || address == null){
            Address adr = address.get(0);
            if(adr != null) {
                int n = adr.getMaxAddressLineIndex();
                addr = new String[n + 1];
                for (int j = 0; j <= n; j++) {
                    addr[j] = adr.getAddressLine(j);
                }
            }
            else{
                addr = new String[]{"None"};
            }
        }
        else{
            addr = new String[]{"None"};
        }
        return addr[0];
    }

    private void sendData()
    {
        StringRequest request = new StringRequest(Request.Method.POST,
                sendurl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Intent intnt = new Intent("response_update");
                intnt.putExtra("Main", datamain);
                sendBroadcast(intnt);
                //gpsText.setText(response);
                /*try {
                    JSONObject jsonObject = new JSONObject(response);
                    Double latid = jsonObject.getDouble("Latitude");
                    Double longid = jsonObject.getDouble("Longitude");
                    Toast.makeText(getApplicationContext(), "Latitude : " + latid + "Longitude : " + longid, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }*/
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Intent intnt = new Intent("response_update");
                intnt.putExtra("Main", datamain);
                intnt.putExtra("error", error.getCause().toString());
                sendBroadcast(intnt);
                //gpsText.setText(error.getCause().toString());
            }
        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                for(int i = 0; i < 9; i++) {
                    params.put(keys[i], datamain[i]);
                }
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void initQueue(){
        Cache cache = new DiskBasedCache(getCacheDir(), 1024*1024);
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
    }
}
