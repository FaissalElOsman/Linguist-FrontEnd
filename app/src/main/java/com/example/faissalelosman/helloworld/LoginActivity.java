package com.example.faissalelosman.helloworld;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    Button bLogin,bRegister;
    EditText etUsername, etPassword;
    static String Token;
    String sLogin,sPassword;
    ProgressBar pbBar;
    Intent iMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername  = (EditText)findViewById(R.id.etUsername);
        etPassword  = (EditText)findViewById(R.id.etPassword);
        bLogin      = (Button)findViewById(R.id.bLogin);
        pbBar       = (ProgressBar) this.findViewById(R.id.progressBar);
        bRegister   = (Button)findViewById(R.id.bRegister) ;

        etPassword.setText("06387475");
        etUsername.setText("faissal.elosman@gmail.com");

        bLogin.setOnClickListener(this);
        bRegister.setOnClickListener(this);

        iMain=new Intent(this,MainActivity.class);
    }

    @Override
    public void onClick(View view) {
        Uri.Builder builder;
        URL url= null;
        switch (view.getId()) {
            case R.id.bLogin:
                builder=new Uri.Builder();
                builder.scheme("https")
                        .authority("faissal-simple-server.herokuapp.com")
                        .appendPath("login")
                        .appendQueryParameter("email",      etUsername.getText().toString())
                        .appendQueryParameter("password",   etPassword.getText().toString());
                try {
                    url = new URL(builder.build().toString());
                    new LoginHandler().execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bRegister:
                builder=new Uri.Builder();
                builder.scheme("https")
                        .authority("faissal-simple-server.herokuapp.com")
                        .appendPath("registerapp");
                try {
                    url         = new URL(builder.build().toString());
                    sLogin      = etUsername.getText().toString();
                    sPassword   = etPassword.getText().toString();
                    new RegisterHandler().execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public class LoginHandler extends AsyncTask<URL,String,String>{
        @Override
        protected void onPreExecute(){
            pbBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... params) {
            HttpURLConnection connection    =null;
            BufferedReader reader           =null;
            try{
                connection = (HttpURLConnection) params[0].openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader              = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line="";
                while((line=reader.readLine())!=null)
                    buffer.append(line);
                connection.disconnect();
                return buffer.toString();
            } catch(IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pbBar.setVisibility(View.GONE);
            try {
                JSONObject object=new JSONObject(s);
                String success = object.getString("success");

                if(success=="true"){
                    Token= object.getString("token");
                    Context context = getApplicationContext();
                    startActivity(iMain);
                }
                else{
                    String data = object.getString("data");
                    Context context = getApplicationContext();
                    Toast.makeText(context, data, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public class RegisterHandler extends AsyncTask<URL,String,String>{
        @Override
        protected void onPreExecute(){
            pbBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... params) {
            HttpURLConnection connection=null;
            BufferedReader reader=null;
            try{
                connection = (HttpURLConnection) params[0].openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                JSONObject object   = new JSONObject();
                try {
                    object.put("email",sLogin);
                    object.put("password",sPassword);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write(object.toString());
                wr.flush();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader=new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer=new StringBuffer();
                String line="";
                while((line=reader.readLine())!=null)
                    buffer.append(line);
                connection.disconnect();
                return buffer.toString();
            } catch(IOException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pbBar.setVisibility(View.GONE);
            try {
                JSONObject object=new JSONObject(s);
                String success = object.getString("success");

                if(success=="true"){
                    Token= object.getString("token");
                    Context context = getApplicationContext();
                    Toast.makeText(context, "User is registered", Toast.LENGTH_LONG).show();

                }
                else{
                    String data = object.getString("data");
                    Context context = getApplicationContext();
                    Toast.makeText(context, data, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

