package com.example.faissalelosman.Linguist;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class Select extends AppCompatActivity implements View.OnClickListener{

    RadioGroup rgInterpreters;
    Button bCall,bBack;
    String sId;
    Intent iMain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        String FirstName,LastName,cost,Text;
        rgInterpreters=(RadioGroup)findViewById(R.id.rgInterpreters);
        rgInterpreters.removeAllViews();
        bCall=(Button)findViewById(R.id.bCall);
        bCall.setOnClickListener(this);
        bBack=(Button)findViewById(R.id.bBack);
        bBack.setOnClickListener(this);
        iMain=new Intent(this, MainActivity.class);
        try {
            Log.d("AppFaissal","\"interpreters\":"+MainActivity.sInterpreters);
            JSONObject object=new JSONObject("{\"interpreters\":"+MainActivity.sInterpreters+"}");
            //JSONObject object=new JSONObject('{"interpreters":[{"fitting_estimation":34,"last_name":"TLAISS","id":3,"languages":[{"power":5,"name":"arabic"},{"power":3,"name":"chinese"}],"first_name":"Ziad","cost":1.25}]}');
            JSONArray interpreters = object.getJSONArray("interpreters");
            for (int i = 0; i < interpreters.length(); i++) {
                RadioButton radioButton=new RadioButton(getApplicationContext());
                JSONObject interpreter = interpreters.getJSONObject(i);
                FirstName=interpreter.getString("first_name");
                LastName=interpreter.getString("last_name");
                radioButton.setTag(interpreter.getString("id"));
                cost=interpreter.getString("cost");
                Text=FirstName+ " "+LastName+"\nCost:"+cost;
                JSONArray languages= interpreter.getJSONArray("languages");
                for(int j=0;j<languages.length();j++){
                    JSONObject language=languages.getJSONObject(j);
                    Text+="\nlanguage: "+language.getString("name")+" with a fluency of "+language.getString("power");
                }
                Text+="\n\n";
                radioButton.setText(Text);
                radioButton.setTextColor(Color.BLACK);
                rgInterpreters.addView(radioButton);
                Text="";
            }
            Log.d("AppFaissal","Child count1= "+rgInterpreters.getChildCount());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        Uri.Builder builder;
        URL url= null;
        switch (view.getId()) {
            case R.id.bCall:
                Log.d("AppFaissal","Child count2= "+rgInterpreters.getChildCount());
                try {
                    for(int i=0;i<rgInterpreters.getChildCount();i++){
                        RadioButton radioButton=(RadioButton)rgInterpreters.getChildAt(i);
                        if(radioButton.isChecked()){
                            sId=radioButton.getTag().toString();
                            break;
                        }
                    }

                    builder = new Uri.Builder();
                    builder.scheme("https")
                            .authority("linguist-backend.herokuapp.com")
                            .appendPath("call")
                            .appendQueryParameter("token",LoginActivity.Token)
                            .appendQueryParameter("translator_id",sId);
                    url = new URL(builder.build().toString());
                    new CallHandler().execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bBack:
                startActivity(iMain);
        }
    }
    public class CallHandler extends AsyncTask<URL,String,String> {
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
                        Context context = getApplicationContext();
                        Toast.makeText(context, "Calling ...", Toast.LENGTH_LONG).show();
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
