package com.themukobeta.mukobeta;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by abhi on 17/3/15.
 */
public class MobileInterface {
    private static final String TAG = "MobileInterface Wear ->";
    private static final String SHOW_SONGS_ON_WATCH = "SHOW_SONGS_ON_WATCH";
    private static final String CHANGE_SONG_ON_WATCH = "CHANGE_SONG_ON_WATCH";

    MainActivity mainActivity;
    public MobileInterface(Activity activity) {
        this.mainActivity = (MainActivity) activity;
        activateReceiver();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String songsList = intent.getStringExtra("songsList");
            mainActivity.handleSongsList(songsList);
            Log.i(TAG," songsList: " + songsList);
        }
    };

    private BroadcastReceiver changeSongReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int songPosition = intent.getIntExtra("songPosition", 0);
            mainActivity.setPage(songPosition);
            Log.i(TAG, "songPosition: " + songPosition);
        }
    };

    private void activateReceiver(){
        LocalBroadcastManager.getInstance(mainActivity).registerReceiver(mMessageReceiver,
                new IntentFilter(SHOW_SONGS_ON_WATCH));

        LocalBroadcastManager.getInstance(mainActivity).registerReceiver(changeSongReceiver,
                new IntentFilter(CHANGE_SONG_ON_WATCH));
    }
}
