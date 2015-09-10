package com.themukobeta.mukobeta.services;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.themukobeta.mukobeta.application.MainApplication;

import java.util.List;

public class WearListener extends WearableListenerService {
    private final String TAG = "WearListener -> ";

    private static final String SEARCH_QUERY_ON_PHONE = "/searchQuery";
    private static final String PLAY_PAUSE_ON_PHONE = "/playPause";
    private static final String CHANGE_SONG_ON_PHONE = "/changeSong";

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate called");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.i(TAG, "onDataChanged called");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().equals(CHANGE_SONG_ON_PHONE)) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    Log.i(TAG," songPosition: " + dataMap.getInt("songPosition"));
                    sendBroadcastToActivity(dataMap.getInt("songPosition"));
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
        super.onDataChanged(dataEvents);
    }

    private void sendBroadcastToActivity(int songPosition){
        Intent intent = new Intent(MainApplication.CHANGE_SONG);
        intent.putExtra("songPosition", songPosition);
        sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i(TAG,"onMessageReceived Path :" + messageEvent.getPath());
        super.onMessageReceived(messageEvent);
        switch (messageEvent.getPath()){
            case SEARCH_QUERY_ON_PHONE:
                Log.i(TAG,"Search Query on phone");
                String query = new String(messageEvent.getData());
                if (isAppInForeground(getApplicationContext())){
                    sendBroadcast(query);
                }
                break;

            case PLAY_PAUSE_ON_PHONE:
                Log.i(TAG,"Play/pause on phone");
                sendPlayPauseBroadcast();
                break;
            default:
                break;
        }
    }

    private void sendPlayPauseBroadcast(){
        Intent broadcast = new Intent();
        broadcast.setAction(MainApplication.SWITCH_PLAY);
        sendBroadcast(broadcast);
    }

    private void sendBroadcast(String query){
        Intent broadcast = new Intent();
        broadcast.setAction(MainApplication.BROADCAST_QUERY_FROM_WEAR);
        broadcast.putExtra("WearQuery",query);
        sendBroadcast(broadcast);
    }

    private boolean isAppInForeground(Context context){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);

        final String myPackage = context.getPackageName();
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if(componentInfo.getPackageName().equals(myPackage)){
            return true;
        }

        return false;
    }
}
