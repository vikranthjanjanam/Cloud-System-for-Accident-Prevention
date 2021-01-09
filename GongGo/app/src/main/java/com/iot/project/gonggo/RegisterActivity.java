package com.iot.project.gonggo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener{

    private EditText name, vhid, pass, conpass, phone;
    private String dateob, phn, vid, nm;
    private String token, url;
    private String[] keys, data;
    private TextView dobLabel, signin;
    private Button register, dob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if(SharedPrefManager.getInstance(this).isLoggedin()){
            Intent main = new Intent(RegisterActivity.this, MainActivity.class);
            main.putExtra("FromStart", "Start");
            startActivity(main);
            finish();
        }

        registerToken();

        dob = findViewById(R.id.dob);
        register = findViewById(R.id.register);

        name = findViewById(R.id.name);
        vhid = findViewById(R.id.vhid);
        pass = findViewById(R.id.pass);
        conpass = findViewById(R.id.conpass);
        phone = findViewById(R.id.regMobile);

        signin = findViewById(R.id.signin);
        dobLabel = findViewById(R.id.DOBLabel);

        keys = new String[]{"Name", "Vehicle", "DOB", "Mobile", "Token", "Password"};
        data = new String[6];
        url = Constants.PublicIP + "register.php";

        dob.setOnClickListener(this);
        signin.setOnClickListener(this);
        register.setOnClickListener(this);
        /*{
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePicker();
                datePicker.show(getSupportFragmentManager(), "Date of Birth Picker");
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish();
                Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(login);
                //finish();
            }
        });*/
    }

    private void registerUser() {

        nm = name.getText().toString().trim();
        phn = phone.getText().toString().trim();
        String ps = pass.getText().toString();
        String cps = conpass.getText().toString();
        vid = vhid.getText().toString().trim();
        token = FirebaseInstanceId.getInstance().getToken();

        if(token == null){
            registerToken();
            Toast.makeText(getApplicationContext(), "Please Connect to Internet", Toast.LENGTH_SHORT).show();
            return;
        }else if(nm.equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter Name", Toast.LENGTH_SHORT).show();
            return;
        }else if(vid.equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter Vehicle ID", Toast.LENGTH_SHORT).show();
            return;
        }else if(dateob.equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter Date of Birth", Toast.LENGTH_SHORT).show();
            return;
        }else if(phn.equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter Mobile Number", Toast.LENGTH_SHORT).show();
            return;
        }else if(ps.equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }else if(cps.equals("")){
            Toast.makeText(getApplicationContext(), "Please Confirm Password", Toast.LENGTH_SHORT).show();
            return;
        }else if(!ps.equals(cps)){
            Toast.makeText(getApplicationContext(), "Passwords doesn't match", Toast.LENGTH_SHORT).show();
            return;
        }

        data[0] = nm;
        data[1] = vid;
        data[2] = dateob;
        data[3] = phn;
        data[4] = token;
        data[5] = ps;

        sendRequest();
    }

    private void sendRequest() {
        StringRequest request = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if(!jsonObject.getBoolean("error")){
                            SharedPrefManager.getInstance(getApplicationContext())
                                    .userLogin(jsonObject.getString("Mobile"),
                                            jsonObject.getString("Vehicle"),
                                            jsonObject.getString("Name"));
                            Intent main = new Intent(RegisterActivity.this, MainActivity.class);
                            main.putExtra("FromStart", "Start");
                            startActivity(main);
                            finish();
                        }else{
                            Toast.makeText(
                                    getApplicationContext(),
                                    (CharSequence) jsonObject.get("message"),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                    /*intent.putExtra("From", "Start");
                    intent.putExtra("Mobile", phn);
                    intent.putExtra("Name", nm);
                    intent.putExtra("Vehicle", vid);*/
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(
                        getApplicationContext(),
                        error.getMessage(),
                        Toast.LENGTH_LONG
                    ).show();
                }
            }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                for(int i = 0; i < 6; i++) {
                    params.put(keys[i], data[i]);
                }
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);
    }

    public void registerToken(){
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        token = FirebaseInstanceId.getInstance().getToken();
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        dateob = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", dayOfMonth);
        dobLabel.setText(dateob);
    }

    @Override
    public void onClick(View view) {
        if (view == dob){
            DialogFragment datePicker = new DatePicker();
            datePicker.show(getSupportFragmentManager(), "Date of Birth Picker");
        }
        else if(view == register){
            registerUser();
        }
        else if(view == signin){
            Intent login = new Intent(RegisterActivity.this, SigninActivity.class);
            startActivity(login);
            //finish();
        }
    }
}
