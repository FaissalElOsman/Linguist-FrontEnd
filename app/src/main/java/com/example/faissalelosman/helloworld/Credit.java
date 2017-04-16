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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Credit extends AppCompatActivity implements View.OnClickListener{
    Button bCharge,bBack;
    EditText etAmount;
    TextView tvCredit;
    ProgressBar pbBar;
    Intent iMain;
    int amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);

        bCharge     = (Button)findViewById(R.id.bCharge);
        bBack       = (Button)findViewById(R.id.bBack);
        etAmount    = (EditText)findViewById(R.id.etAmount);
        tvCredit    = (TextView)findViewById(R.id.tvCredit);
        pbBar       = (ProgressBar)findViewById(R.id.pbBar);

        bCharge.setOnClickListener(this);
        bBack.setOnClickListener(this);

        iMain=new Intent(this, MainActivity.class);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("faissal-simple-server.herokuapp.com")
                .appendPath("getCredit")
                .appendQueryParameter("token", LoginActivity.Token.toString());
        URL url = null;
        try {
            url = new URL(builder.build().toString());
            new CreditHandler().execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bCharge:
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("faissal-simple-server.herokuapp.com")
                        .appendPath("updateCredit")
                        .appendQueryParameter("token", LoginActivity.Token.toString())
                        .appendQueryParameter("credit", etAmount.getText().toString());
                URL url = null;
                try {
                    url = new URL(builder.build().toString());
                    new CreditHandler().execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bBack:
                startActivity(iMain);
                break;
        }
    }
    public class CreditHandler extends AsyncTask<URL,String,String> {
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
                    JSONObject data=object.getJSONObject("data");
                    String amount =data.getString("credit");
                    tvCredit.setText(amount);
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
