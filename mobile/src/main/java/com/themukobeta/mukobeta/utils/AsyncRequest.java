package com.themukobeta.mukobeta.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.List;

public class AsyncRequest extends AsyncTask<String, Integer, String> {

    OnAsyncRequestComplete caller;
    Context context;
    String method = "GET";
    List<NameValuePair> parameters = null;
    JSONObject params = null;
    /*ProgressDialog pDialog = null;
    boolean showDialog = false;*/

    // Three Constructors
    public AsyncRequest(Service a, String m, List<NameValuePair> p) {
        caller = (OnAsyncRequestComplete) a;
        context = a;
        method = m;
        parameters = p;
    }

    public AsyncRequest(Activity a, String m, List<NameValuePair> p) {
        caller = (OnAsyncRequestComplete) a;
        context = a;
        method = m;
        parameters = p;
    }

    public AsyncRequest(Activity a, String m, JSONObject p) {
        caller = (OnAsyncRequestComplete) a;
        context = a;
        method = m;
        params = p;
    }

    public AsyncRequest(Activity a, String m) {
        caller = (OnAsyncRequestComplete) a;
        context = a;
        method = m;
    }


    public AsyncRequest(Activity a) {
        caller = (OnAsyncRequestComplete) a;
        context = a;
    }

    // Interface to be implemented by calling activity
    public interface OnAsyncRequestComplete {
        public void asyncResponse(String response);
    }

    public String doInBackground(String... urls) {
        // get url pointing to entry point of API
        String address = urls[0].toString();
        if (method == "POST") {
            return post(address);
        }

        if (method == "GET") {
            return get(address);
        }

        if (method == "PUT") {
            put(address);
        }

        return null;
    }

    public void onPreExecute() {
      /*  if (showDialog) {
            pDialog = new ProgressDialog(context);

            pDialog.setMessage("Loading data.."); // typically you will define such
            // strings in a remote file.
            pDialog.show();
        }*/
    }

    public void onProgressUpdate(Integer... progress) {
        // you can implement some progressBar and update it in this record
        // setProgressPercent(progress[0]);
    }

    public void onPostExecute(String response) {
        /*if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }*/
        caller.asyncResponse(response);
    }

    protected void onCancelled(String response) {
     /*   if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }*/
        caller.asyncResponse(response);
    }

    @SuppressWarnings("deprecation")
    private String get(String address) {
        try {

            if (parameters != null) {

                String query = "";
                String EQ = "="; String AMP = "&";
                for (NameValuePair param : parameters) {
                    query += param.getName() + EQ + URLEncoder.encode(param.getValue()) + AMP;
                }

                if (query != "") {
                    address += "?" + query;
                }
            }

            HttpClient client = new DefaultHttpClient();
            HttpGet get= new HttpGet(address);

            HttpResponse response = client.execute(get);
            return stringifyResponse(response);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

        return null;
    }

    private String post(String address) {
        try {

            HttpClient client = new DefaultHttpClient();

            HttpPost post = new HttpPost(address);

            //Log.i("Parameters: ", parameters.toString());
            if (parameters != null) {
                post.setEntity(new UrlEncodedFormEntity(parameters));
            }
            else if (params != null){
                post.setEntity(new StringEntity(params.toString()));
            }
            post.setHeader("Connection","close");


            HttpResponse response = client.execute(post);
            return stringifyResponse(response);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

        return null;
    }

    private String put(String address) {
        try {

            HttpClient client = new DefaultHttpClient();

            HttpPut put = new HttpPut(address);

            if (parameters != null) {
                put.setEntity(new UrlEncodedFormEntity(parameters));
            }

            HttpResponse response = client.execute(put);
            return stringifyResponse(response);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

        return null;
    }

    private String stringifyResponse(HttpResponse response) {
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();

            return sb.toString();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
}