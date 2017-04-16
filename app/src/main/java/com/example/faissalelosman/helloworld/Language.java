package com.example.faissalelosman.helloworld;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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

public class Language extends AppCompatActivity implements View.OnClickListener{

    Button bAdd,bBack,bRemove;
    EditText etLanguage;
    SeekBar sbFluency;
    TableLayout tlLanguages;
    ProgressBar pbBar;
    Context context;
    Intent iMain;
    String sLanguage,sLevel;
    JSONObject request;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        etLanguage=(EditText)findViewById(R.id.etLanguage);
        bAdd=(Button)findViewById(R.id.bAdd);
        bBack=(Button)findViewById(R.id.bBack);
        bRemove=(Button)findViewById(R.id.bRemove);
        sbFluency=(SeekBar)findViewById(R.id.sbFluency);
        tlLanguages=(TableLayout)findViewById(R.id.tlLanguages);
        context = getApplicationContext();
        iMain=new Intent(this, MainActivity.class);
        pbBar       = (ProgressBar)findViewById(R.id.pbBar);

        bAdd.setOnClickListener(this);
        bBack.setOnClickListener(this);
        bRemove.setOnClickListener(this);


            for (int i = 0; i < MainActivity.sLanguages.size(); i++) {
                TableRow tableRow = new TableRow(context);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
                tableRow.setLayoutParams(layoutParams);
                CheckBox checkBox = new CheckBox(context);
                checkBox.setTextColor(Color.RED);
                String tmp = MainActivity.sLanguages.get(i);
                String[] arrayOfString = tmp.split(" ");
                checkBox.setText(arrayOfString[0] + " Fluency " + arrayOfString[1]);
                tableRow.addView(checkBox);
                tlLanguages.addView(tableRow, i);
            }


    }

    @Override
    public void onClick(View view) {
        Uri.Builder builder;
        URL url= null;
        switch (view.getId()) {
            case R.id.bAdd:
                builder=new Uri.Builder();
                builder.scheme("https")
                        .authority("faissal-simple-server.herokuapp.com")
                        .appendPath("addLanguage");
                try {
                    url = new URL(builder.build().toString());
                    sLanguage=etLanguage.getText().toString();
                    sLevel=String.valueOf(sbFluency.getProgress());
                    new AddLanguageHandler().execute(url);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bBack:
                startActivity(iMain);
                break;
            case R.id.bRemove:
                request   = new JSONObject();
                JSONArray array=new JSONArray();
                builder=new Uri.Builder();
                builder.scheme("https")
                        .authority("faissal-simple-server.herokuapp.com")
                        .appendPath("removeLanguage");
                try {
                    url = new URL(builder.build().toString());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                String toDelete="";
                int count=tlLanguages.getChildCount();
                for (int i = tlLanguages.getChildCount(); i >-1 ; i--) {
                    View tmp_view = tlLanguages.getChildAt(i);
                    if (tmp_view instanceof TableRow) {
                        View tmp_view_loop = ((TableRow) tmp_view).getVirtualChildAt(0);
                        if (tmp_view_loop instanceof CheckBox){
                            CheckBox tmp_check = (CheckBox) tmp_view_loop;
                            if(tmp_check.isChecked()){
                                String[] arrayOfString = tmp_check.getText().toString().split(" ");
                                toDelete+="'"+arrayOfString[0].toString()+"',";
                                tlLanguages.removeView(tmp_view);
                                MainActivity.sLanguages.remove(i);
                            }
                        }

                    }
                }
                StringBuilder sb = new StringBuilder(toDelete);
                sb.deleteCharAt(toDelete.length()-1);
                toDelete=sb.toString();
                try {
                    request.put("token",LoginActivity.Token);
                    request.put("language",toDelete);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new RemoveLanguageHandler().execute(url);
                break;
        }
    }

    public class RemoveLanguageHandler extends AsyncTask<URL,String,String>{
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

                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write(request.toString());
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
        }
    }

    public class AddLanguageHandler extends AsyncTask<URL,String,String>{
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
                    object.put("fluency",sLevel);
                    object.put("language",sLanguage);

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
                    Toast.makeText(context, "Language is added", Toast.LENGTH_LONG).show();
                    TableRow tableRow= new TableRow(context);
                    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
                    tableRow.setLayoutParams(layoutParams);
                    CheckBox checkBox = new CheckBox(context);
                    checkBox.setTextColor(Color.RED);
                    checkBox.setText(sLanguage+" Fluency "+sLevel);
                    tableRow.addView(checkBox);
                    tlLanguages.addView(tableRow,tlLanguages.getChildCount());
                    String tmp=sLanguage+" "+sLevel;
                    MainActivity.sLanguages.add(tmp);

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
