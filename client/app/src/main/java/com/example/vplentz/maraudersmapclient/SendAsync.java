package com.example.vplentz.maraudersmapclient;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SendAsync extends AsyncTask<JSONObject, Integer, String>{
//    JSONObject json;
    private String TAG = SendAsync.class.getSimpleName();
    private Activity activity;
    public SendAsync(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(JSONObject... jsons) {
        JSONObject myLocJson = jsons[0];
        String result = null;


        try {
            InetAddress inetAddress = InetAddress.getByName("165.227.86.76");
            Socket socket = new Socket(inetAddress,  5000);

            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            out.println(myLocJson);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = null;
                response = in.readLine();
            if(response != null)
                return response;
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    return null;
    }

    protected void onPostExecute(String result){
        Toast.makeText(activity, "Updating", Toast.LENGTH_SHORT).show();
        if(result == null){
            Log.d(TAG, "result is null");
            return;
        }
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "ARRAY" + jsonArray.toString());
        ((MapsActivity) activity).showLocations(jsonArray);
    }
}
