package com.themukobeta.mukobeta;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class WatchListener extends WearableListenerService {
    private static final String TAG = "MukoApp Wear: WatchListener ->";
    private static final String SONGS_LIST = "/songslist";
    private static final String CHANGE_SONG_ON_WEAR = "/changeSongOnWear";
    private static final String SHOW_SONGS_ON_WATCH = "SHOW_SONGS_ON_WATCH";
    private static final String CHANGE_SONG_ON_WATCH = "CHANGE_SONG_ON_WATCH";

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().equals(SONGS_LIST)) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    Log.i(TAG,"songsList: " + dataMap.getString("songsList"));
                    sendBroadcastToActivity(dataMap.getString("songsList"));
                }
                else if (item.getUri().getPath().equals(CHANGE_SONG_ON_WEAR)){
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    Log.i(TAG,"songPosition: " + dataMap.getInt("songPosition"));
                    sendSongChange(dataMap.getInt("songPosition"));
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
    }

    private void sendBroadcastToActivity(String songs){
        Log.i(TAG,"sendBroadcastToActivity");
        Intent intent = new Intent(SHOW_SONGS_ON_WATCH);
        intent.putExtra("songsList",songs);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendSongChange(int songPosition){
        Log.i(TAG,"sendSongChange");
        Intent intent = new Intent(CHANGE_SONG_ON_WATCH);
        intent.putExtra("songPosition",songPosition);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
