package com.norscan.levlertapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.norscan.levlertapp.utils.JsonHelper;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class LoginActivity extends Activity {

    private String baseUrl = "https://www.levlertsecure.com:3001/users/sign_in.json?";
    private Activity context = this;
    ProgressBar pb;
    AutoCompleteTextView email;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pb = (ProgressBar) findViewById(R.id.login_progress);
        email = (AutoCompleteTextView) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
    }

    public void login(View view)
    {
        String inputEmail;
        String inputPassword;

        pb.setVisibility(ProgressBar.VISIBLE);

        //inputEmail = email.getText().toString().trim();
        //inputPassword = password.getText().toString().trim();

        inputPassword = "ONlCSCQcO1T90Co4Yv";
        inputEmail = "garrett.enquist@gmail.com";

        baseUrl += "user[email]=" + inputEmail + "&user[password]=" + inputPassword;

        LoginParser login = new LoginParser();
        login.execute();

    }

    public class LoginParser extends AsyncTask<Void, Void, Void>
    {
        String data;
        @Override
        protected Void doInBackground(Void... voids)
        {
            int responseCode = 0;
            try
            {
                JsonHelper helper = new JsonHelper(context);
                helper.trustEveryone();

                URL url = new URL(baseUrl);

                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();
                responseCode = conn.getResponseCode();
                InputStream stream = conn.getInputStream();

                data = helper.convertStreamToString(stream);

                stream.close();
            } catch (Exception ex)
            {
                Log.e("Something went wrong" + String.valueOf(responseCode), ex.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            String authToken = "";

            try
            {
                JSONObject reader = new JSONObject(data);
                JSONObject data = reader.getJSONObject("data");
                JSONObject signIn = data.getJSONObject("sign_in");

                authToken = signIn.getString("authentication_token");
                authToken += ":";
                String bytesEncoded = Base64.encodeToString(authToken.getBytes(), Base64.DEFAULT);

                SharedPreferences sp = getSharedPreferences("LoginActivity", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("authToken", bytesEncoded);
                editor.commit();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            //It needs some more authentication checking
            pb.setVisibility(ProgressBar.INVISIBLE);
            if (authToken.length() > 5)
            {
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
