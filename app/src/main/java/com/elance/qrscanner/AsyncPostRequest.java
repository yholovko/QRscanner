package com.elance.qrscanner;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AsyncPostRequest extends AsyncTask<Void, Void, String> {
    private String httpAddress;
    private Map<String, String> params;

    public AsyncPostRequest(String httpAddress, Map<String, String> params) {
        this.httpAddress = httpAddress;
        this.params = params;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return sendData(httpAddress, params);
    }

    public static String sendData(String httpAddress, Map<String, String> params) {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(httpAddress);

            List<NameValuePair> nameValuePairs = new ArrayList<>();

            for (Map.Entry<String, String> param : params.entrySet())
                nameValuePairs.add(new BasicNameValuePair(param.getKey(), param.getValue()));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

            HttpResponse response = httpClient.execute(httpPost);
            InputStream ips = response.getEntity().getContent();
            BufferedReader buf = new BufferedReader(new InputStreamReader(ips, "UTF-8"));
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            String s;
            while (true) {
                s = buf.readLine();
                if (s == null || s.length() == 0)
                    break;
                sb.append(s);
            }
            buf.close();
            ips.close();
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }
}