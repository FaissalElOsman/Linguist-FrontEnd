package com.example.faissalelosman.helloworld;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Update extends AppCompatActivity implements View.OnClickListener{

    Button bUpdate,bBack;
    EditText etFirstName,etLastName,etPhoneNumber,etTranslationFees;
    CheckBox cbTranslator,cbUser;
    ProgressBar pbBar;
    int profile;
    Intent iMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        bUpdate             = (Button)findViewById(R.id.bUpdate);
        bBack               = (Button)findViewById(R.id.bBack);
        etFirstName         = (EditText)findViewById(R.id.etFirstName);
        etLastName          = (EditText)findViewById(R.id.etLastName);
        etPhoneNumber       = (EditText)findViewById(R.id.etPhoneNumber);
        etTranslationFees   = (EditText)findViewById(R.id.etTranslationFees);
        cbTranslator        = (CheckBox)findViewById(R.id.cbTranslator);
        cbUser              = (CheckBox)findViewById(R.id.cbUser);
        pbBar               = (ProgressBar)findViewById(R.id.pbBar);

        etFirstName.setText(MainActivity.sFirstName);
        etLastName.setText(MainActivity.sLastName);
        etPhoneNumber.setText(MainActivity.sPhoneNumber);
        etTranslationFees.setText(MainActivity.sTranslationFees);
        cbTranslator.setChecked(MainActivity.bIsIntertpreter);
        cbUser.setChecked(MainActivity.bIsUser);

        bUpdate.setOnClickListener(this);
        bBack.setOnClickListener(this);
        iMain=new Intent(this, MainActivity.class);
    }

    @Override
    public void onClick(View view) {
        Uri.Builder builder;
        URL url= null;
        switch (view.getId()) {
            case R.id.bUpdate:
                MainActivity.sFirstName         = etFirstName.getText().toString();
                MainActivity.sLastName          = etLastName.getText().toString();
                MainActivity.sPhoneNumber       = etPhoneNumber.getText().toString();
                MainActivity.sTranslationFees   = etTranslationFees.getText().toString();

                MainActivity.bIsIntertpreter    = cbTranslator.isChecked();
                MainActivity.bIsUser            = cbUser.isChecked();

                profile = 0;
                if(cbTranslator.isChecked()&&cbUser.isChecked())    profile=2;
                else if(cbTranslator.isChecked())                   profile=1;

                builder=new Uri.Builder();
                builder.scheme("https")
                        .authority("linguist-backend.herokuapp.com")
                        .appendPath("update");
                try {
                    url = new URL(builder.build().toString());
                    new UpdateHandler().execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bBack:
                startActivity(iMain);
                break;
        }
    }

    public class UpdateHandler extends AsyncTask<URL,String,String> {
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
                    object.put("token",LoginActivity.Token.toString());
                    object.put("first_name",MainActivity.sFirstName);
                    object.put("last_name",MainActivity.sLastName);
                    object.put("phone_number",MainActivity.sPhoneNumber);
                    object.put("translation_fees",MainActivity.sTranslationFees);
                    object.put("profile", ""+profile);

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
                    Context context = getApplicationContext();
                    Toast.makeText(context, "User is updated", Toast.LENGTH_LONG).show();

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
