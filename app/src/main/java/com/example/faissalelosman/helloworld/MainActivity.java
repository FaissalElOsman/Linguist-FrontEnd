package com.example.faissalelosman.helloworld;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button bUpdateUser,bManageLanguage,bCredit,bGo,bLogout;
    EditText etLanguage;
    ProgressBar pbBar;
    Intent iUpdateUser,iLanguage,iCredit,iLogin,iSelect;
    static String sFirstName, sLastName, sPhoneNumber,sInterpreters,sTranslationFees;
    static boolean bIsIntertpreter=false,bIsUser=false;
    static List<String> sLanguages = new ArrayList<String>();
    static Boolean bFirstTime=true;

    @Override
    public void onClick(View view) {
        Uri.Builder builder;
        URL url = null;
        switch (view.getId()){
            case R.id.bUpdateUser:
                startActivity(iUpdateUser);
                break;
            case R.id.bManageLanguage:
                startActivity(iLanguage);
                break;
            case R.id.bCredit:
                startActivity(iCredit);
                break;
            case R.id.bGo:
                builder=new Uri.Builder();
                builder.scheme("https")
                        .authority("linguist-backend.herokuapp.com")
                        .appendPath("getTranslator")
                        .appendQueryParameter("token",LoginActivity.Token.toString())
                        .appendQueryParameter("language",etLanguage.getText().toString());
                try {
                    url = new URL(builder.build().toString());
                    new GoHandler().execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bLogout:
                bFirstTime=true;
                sLanguages.clear();
                builder=new Uri.Builder();
                builder.scheme("https")
                        .authority("linguist-backend.herokuapp.com")
                        .appendPath("logout")
                        .appendQueryParameter("token",LoginActivity.Token.toString());
                try {
                    url = new URL(builder.build().toString());
                    new LogoutHandler().execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bUpdateUser     = (Button)findViewById(R.id.bUpdateUser);
        bManageLanguage = (Button)findViewById(R.id.bManageLanguage);
        bCredit         = (Button)findViewById(R.id.bCredit);
        bGo             = (Button)findViewById(R.id.bGo);
        bLogout         = (Button)findViewById(R.id.bLogout);
        etLanguage      = (EditText)findViewById(R.id.etLanguage);
        pbBar           = (ProgressBar)findViewById(R.id.pbBar);

        iUpdateUser     = new Intent(this,Update.class);
        iLanguage       = new Intent(this,Language.class);
        iCredit         = new Intent(this,Credit.class);
        iLogin          = new Intent(this,LoginActivity.class);
        iSelect         = new Intent(this,Select.class);

        bUpdateUser.setOnClickListener(this);
        bManageLanguage.setOnClickListener(this);
        bCredit.setOnClickListener(this);
        bGo.setOnClickListener(this);
        bLogout.setOnClickListener(this);

        if(bFirstTime) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("linguist-backend.herokuapp.com")
                    .appendPath("getProfile")
                    .appendQueryParameter("token", LoginActivity.Token.toString());
            URL url = null;
            try {
                url = new URL(builder.build().toString());
                new GetProfileHandler().execute(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            bFirstTime = false;
        }
    }
    public class GetProfileHandler extends AsyncTask<URL,String,String> {

        @Override
        protected String doInBackground(URL... params) {
            HttpURLConnection connection=null;
            BufferedReader reader=null;
            try{
                connection = (HttpURLConnection) params[0].openConnection();
                connection.setRequestMethod("GET");
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
            try {
                JSONObject object=new JSONObject(s);
                String success = object.getString("success");

                if(success=="true"){
                    JSONObject data=object.getJSONObject("data");
                    sFirstName=data.getString("first_name");
                    sLastName=data.getString("last_name");
                    sPhoneNumber=data.getString("phone_number");
                    if(data.getString("profile")!="null"){
                        int profile= Integer.parseInt(data.getString("profile"));
                        if(profile>0)
                            bIsIntertpreter=true;
                        if(profile<2)
                            bIsUser=true;
                    }

                    sTranslationFees=data.getString("translation_fees");

                    JSONArray languages = data.getJSONArray("languages");
                    for (int i = 0; i < languages.length(); i++) {
                        JSONObject language = languages.getJSONObject(i);
                        String tmp=language.getString("language")+" "+language.getString("fluency");
                        sLanguages.add(tmp);
                    }
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

    public class LogoutHandler extends AsyncTask<URL,String,String> {
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
                connection.setRequestMethod("GET");
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
                    startActivity(iLogin);
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

    public class GoHandler extends AsyncTask<URL,String,String> {
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
                connection.setRequestMethod("GET");
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
                    sInterpreters=object.getString("data");
                    startActivity(iSelect);
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
