package com.example.vplentz.maraudersmapclient;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class SendAsync extends AsyncTask<JSONObject, Integer, byte[]>{
//    JSONObject json;
    private String TAG = SendAsync.class.getSimpleName();
    private Activity activity;
    private DatagramPacket recvedPack;
    public SendAsync(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected byte[] doInBackground(JSONObject... jsons) {
        byte[] lMsg = new byte[1024];
        Log.d(TAG, "SENDIND DATA");
        JSONObject myLocJson = jsons[0];
        try {
            DatagramSocket mSocket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(myLocJson.toString().getBytes(), myLocJson.toString().getBytes().length,
                    InetAddress.getByName("165.227.86.76"), 5000);
//                    InetAddress.getByName("192.168.1.8"), 5000);
            mSocket.send(packet);
            mSocket.setSoTimeout(1000);
            recvedPack = new DatagramPacket(lMsg, lMsg.length);
            while(true){        // recieve data until timeout
                try {
                    mSocket.receive(recvedPack);
                    Log.d(TAG, "recvedin back");
                    return lMsg;
                }
                catch (SocketTimeoutException e) {
                    // timeout exception.
                    System.out.println("Timeout reached!!! " + e);
                    mSocket.close();
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    protected void onPostExecute(byte[] result){Log.d(TAG, "receveid some data");
        Toast.makeText(activity, "Updating", Toast.LENGTH_SHORT).show();
        if(result == null){
            Log.d(TAG, "result is null");
            return;
        }
        String pack = new String(result, 0, result.length);
        if(pack.contains("_id")) {
//            Log.d(TAG, pack);
            Log.d(TAG, "Pack " +pack.substring(0, pack.lastIndexOf("}]") + 2));
            try {
                JSONArray jsonArray= new JSONArray(pack);
                Log.d(TAG, jsonArray.toString());
                ((MapsActivity) activity).showLocations(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            Log.d(TAG, "RECEIVED NOTHING");
        }

    }
}
