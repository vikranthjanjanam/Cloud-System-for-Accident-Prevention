package com.iot.project.gonggo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class SigninActivity extends AppCompatActivity {

    private EditText phone, pass;
    private Button login;
    private String url;

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        if(SharedPrefManager.getInstance(this).isLoggedin()){
            Intent main = new Intent(SigninActivity.this, MainActivity.class);
            main.putExtra("FromStart", "Start");
            finish();
            startActivity(main);
            return;
        }

        login = findViewById(R.id.signIn);
        phone = findViewById(R.id.signinMobile);
        pass = findViewById(R.id.signinPassword);

        url = Constants.PublicIP + "login.php";

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });
    }

    private void userLogin() {

        token = FirebaseInstanceId.getInstance().getToken();
        final String mobile = phone.getText().toString().trim();
        final String password = pass.getText().toString();

        if(token == null){
            registerToken();
            Toast.makeText(getApplicationContext(), "Please Connect to Internet", Toast.LENGTH_SHORT).show();
            return;
        }else if(mobile.equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter Mobile Number", Toast.LENGTH_SHORT).show();
            return;
        }else if(password.equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

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
                        Intent main = new Intent(SigninActivity.this, MainActivity.class);
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
                params.put("Mobile", mobile);
                params.put("Password", password);
                params.put("Token", token);
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
}
