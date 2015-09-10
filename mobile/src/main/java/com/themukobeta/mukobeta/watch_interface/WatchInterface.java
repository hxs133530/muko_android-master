package com.themukobeta.mukobeta.watch_interface;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.themukobeta.mukobeta.MainActivity;
import com.themukobeta.mukobeta.application.MainApplication;

/**
 * Created by abhi on 17/3/15.
 */
public class WatchInterface {

    private static final String TAG = "MukoAppWatchInterface->";

    MainActivity mainActivity;

    public WatchInterface(Activity activity) {
        mainActivity = (MainActivity) activity;
        activateReceiver();
    }

    //Recieving Broadcast
    private BroadcastReceiver wearReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String wearQuery = intent.getStringExtra("WearQuery");
            mainActivity.handleQueryFromWear(wearQuery);
        }
    };

    private BroadcastReceiver changeSongReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int songPosition = intent.getIntExtra("songPosition", 0);
            Log.i(TAG, "changeSongReceiver : " + songPosition);
            mainActivity.setChangeSongOnWear(true);
            mainActivity.setPage(songPosition);
        }
    };

    private BroadcastReceiver switchPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mainActivity.handlePlayPauseFromWear();
        }
    };

    private void activateReceiver(){
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(MainApplication.BROADCAST_QUERY_FROM_WEAR);
        mainActivity.registerReceiver(wearReceiver, filter1);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(MainApplication.CHANGE_SONG);
        mainActivity.registerReceiver(changeSongReceiver, filter2);

        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(MainApplication.SWITCH_PLAY);
        mainActivity.registerReceiver(switchPlayReceiver, filter3);
    }

    public void deactivateReceivers(){
        mainActivity.unregisterReceiver(wearReceiver);
        mainActivity.unregisterReceiver(changeSongReceiver);
        mainActivity.unregisterReceiver(switchPlayReceiver);
    }
}
