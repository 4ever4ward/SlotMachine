/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.phonegap.helloworld;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.webkit.WebView;

import org.apache.cordova.CordovaActivity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends CordovaActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GetRedirectTask redirectTask = new GetRedirectTask();
        redirectTask.execute();

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        try {

            String redirectUrl = redirectTask.get();

            if (redirectUrl.equals("")) {
                // Set by <content src="index.html" /> in config.xml
                loadUrl(launchUrl);
            } else {

                Log.e(TAG, "onCreate: " + redirectUrl);

                setContentView(R.layout.activity_main);
                WebView webView = (WebView) findViewById(R.id.webView);

                webView.loadUrl("http://google.com.ua");
                finish();

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    private class GetRedirectTask extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            return getRedirect();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

    }

    private String getRedirect() {
        final String API_URL = "http://topslots.ru/api/login";
        final String PARAM_UUID = "uuid";
        String redirectStr = "";

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(API_URL);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair(PARAM_UUID, getUUID()));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            String jsonStr = inputStreamToString(response.getEntity().getContent());

            redirectStr = getRedirectUrlFromJson(jsonStr);

        } catch (ClientProtocolException e) {
            Log.e(TAG, "getRedirect: Problem with Internet connection", e);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "getRedirect: UnsupportedEncodingException", e);
        } catch (IOException e) {
            Log.e(TAG, "getRedirect: IOException", e);
        } catch (JSONException e) {
            Log.e(TAG, "getRedirect: JSONException", e);
        }

        return redirectStr;
    }

    private String getRedirectUrlFromJson(String jsonStr) throws JSONException {
        final String RESPONSE = "response";
        final String REDIRECT = "redirect";
        final String PARAM_URL = "url";

        JSONObject redirectJSONObject = new JSONObject(jsonStr)
                .getJSONObject(RESPONSE)
                .getJSONObject(REDIRECT);

        return redirectJSONObject.getString(PARAM_URL);
    }

    private String getUUID() {
        String device_uuid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String packageName = getPackageName();

        //TODO: change to app bundle_id
        return "com.stuod" + "::" + device_uuid;
    }

    private String inputStreamToString(InputStream is) throws IOException {
        String line;
        StringBuilder total = new StringBuilder();

        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        while ((line = rd.readLine()) != null)
            total.append(line);

        return total.toString();
    }

}
