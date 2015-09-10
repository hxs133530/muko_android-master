package com.themukobeta.mukobeta.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;
import com.themukobeta.mukobeta.MainActivity;
import com.themukobeta.mukobeta.R;
import com.themukobeta.mukobeta.application.MainApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MusicService extends Service {
    private static final String TAG = "MukoApp:MusicService->";
    private static final int NOTIFY_ID = 1;

    /**
     * Songs playlist
     */
    private JSONArray songsList;

    /**
     * Current song object
     */
    private JSONObject currentTrack;

    /**
     * Android built-in player
     */
    private MediaPlayer player, nextPlayer;

    /**
     * Spotify Player
     */
    private Player mPlayer;

    /**
     * Current song position
     */
    private int songPosition = 0;


    private final IBinder musicServiceBinder = new MusicServiceBinder();

    //private Thread thread; to load next song
    private boolean threadRunning = false;
    AsyncTask<Void,Void,Void> task;

    NotificationManager notificationManager;
    RemoteViews notificationView;

    Notification notification;

    /**
     * Player paused
     */
    private boolean isPaused = false;

    /**
     * Spotify player state
     */
    PlayerState mPlayerState;

    public MusicService() {
    }

    /**
     * Set Spotify Player
     * @param mPlayer
     */
    public void setmPlayer(Player mPlayer) {
        this.mPlayer = mPlayer;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy called");
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }

        if (mPlayer != null){
            mPlayer.pause();
            mPlayer.shutdownNow();
            Spotify.destroyPlayer(mPlayer);
        }
        deactivateNotificationReceivers();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate called");

        activateNotificationReceivers();
    }

    public JSONObject getCurrentTrack() {
        JSONObject track;
        try {
            track = (JSONObject) songsList.get(songPosition);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return track;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*Log.i(TAG, "onStartcommand called");
        try {
            songsList = new JSONArray(intent.getStringExtra("songList"));
        }
        catch (Exception e){
            Log.i(TAG, "onStartcommand no results received -> " + e.getMessage());
        }*/
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "MusicService: onBind() called");
        return musicServiceBinder;
    }


    public void loadSongAsync(boolean manualPlay){
        //Background thread to buffer next song
        /*Thread thread = new Thread() {
            @Override
            public void run() {
                if(player != null){
                    player.release();
                    player = null;
                }
                if(nextPlayer != null){
                    nextPlayer.release();
                    nextPlayer = null;
                }

                try {
                    JSONObject track = (JSONObject) songsList.get(songPosition);
                    Log.i(TAG, "loadSongAsync rdioId -> " + track.getString("rdioId"));
                    player = rdio.getPlayerForTrack(track.getString("rdioId"), null, false);
                    player.prepare();
                }
                catch (Exception e){

                }
            }
        };
        thread.start();*/
        Log.i(TAG,"loadAsync called");
        if (!manualPlay) {
            Log.i(TAG,"Send broadcast to activity");
            Intent intent = new Intent(MainApplication.CHANGE_SONG);
            intent.putExtra("songPosition", songPosition);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return;
        }

        if (threadRunning){
            Log.i(TAG,"LoadAsync Thread interrupted");
            //thread.interrupt();
            task.cancel(true);
            task = null;
            threadRunning = false;
        }

        task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Log.i(TAG,"song url: " + songPosition);
                    player = new MediaPlayer();
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setDataSource(currentTrack.getString("pUrl"));

                    player.prepareAsync();
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            playSong();
                        }
                    });
                    if (isCancelled()){
                        Log.i(TAG,"isCancelled check");
                        return null;
                    }

                }
                catch (Exception e){
                    Log.i(TAG, "songPosition exception caught: " + e.getMessage());
                    nextSong();
                    loadSongAsync(false);
                    //showToastMessage(getResources().getString(R.string.song_unplayable));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.i(TAG,"onPostExecute loadAsync");
                threadRunning = false;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                threadRunning = true;
                if(player != null){
                    if (player.isPlaying()) {
                        player.stop();
                    }
                    player.release();
                    player = null;
                }
                /*if(nextPlayer != null){
                    nextPlayer.release();
                    nextPlayer = null;
                }*/

                try {
                    Log.i(TAG,"onPreExecute songpos: " + songPosition);
                    currentTrack = songsList.getJSONObject(songPosition);
                    updateSongOnNotification();
                }
                catch (Exception e){
                    Log.i(TAG,"onPreExecute exception");
                }
            }
        };

        task.execute();
    }

    public void playSong(){
        Log.i(TAG, "playSong -> " + songPosition);

      /*  if (!manualPlay) {
            Log.i(TAG,"Send broadcast to activity");
            Intent intent = new Intent(MainApplication.CHANGE_SONG);
            intent.putExtra("songPosition", songPosition);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return;
        }*/

        try {
           /* if (nextPlayer != null){
                player = nextPlayer;
                nextPlayer = null;
            }
            else if (!manualPlay){
                Log.i(TAG,"Prepare next song");
                JSONObject track = (JSONObject) songsList.get(songPosition);
                Log.i(TAG, "rdioId -> " + track.getString("rdioId"));
                player = rdio.getPlayerForTrack(track.getString("rdioId"), null, manualPlay);
                player.prepare();
            }*/


            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //mp.reset();
                    nextSong();
                    loadSongAsync(false);
                }
            });

            player.start();
        }
        catch (Exception e){
            Log.e(TAG,"playSong exception -> " + e.getMessage());
        }


        /*if (threadRunning){
            Log.i(TAG,"PlaySong Thread interrupted");
            //thread.interrupt();
            task.cancel(true);
            threadRunning = false;
        }*/

        /*//Background thread to buffer next song
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    nextSong();
                    threadRunning = true;
                    JSONObject track = (JSONObject) songsList.get(songPosition);
                    if(nextPlayer != null){
                        nextPlayer.release();
                        nextPlayer = null;
                    }
                    Log.i(TAG, "AsyncTask rdioId -> " + track.getString("rdioId"));
                    nextPlayer = rdio.getPlayerForTrack(track.getString("rdioId"), null, false);
                    nextPlayer.prepare();
                }
                catch (Exception e){

                }
            }
        };
        thread.start();*/

        /*task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    nextSong();
                    threadRunning = true;
                    JSONObject track = (JSONObject) songsList.get(songPosition);

                    Log.i(TAG, "AsyncTask rdioId -> " + track.getString("rdioId"));
                    nextPlayer = rdio.getPlayerForTrack(track.getString("rdioId"), null, false);
                    nextPlayer.prepare();
                }
                catch (Exception e){

                }
                if (isCancelled()){
                    Log.i(TAG,"isCancelled check");
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                threadRunning = false;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                threadRunning = true;
                if(nextPlayer != null){
                    nextPlayer.release();
                    nextPlayer = null;
                }
            }
        };

        task.execute();*/

    }

    public void playSpotifyPlayer() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }

        try {
            currentTrack = songsList.getJSONObject(songPosition);
            mPlayer.getPlayerState(new PlayerStateCallback() {
                @Override
                public void onPlayerState(PlayerState playerState) {
                    mPlayerState = playerState;
                    Log.i(TAG,"mplayerstate callback: " + mPlayerState.playing);
                    Log.i(TAG,"playerstate callback: " + playerState.playing);
                }
            });
            mPlayer.play("spotify:track:" + currentTrack.getString("sId"));
            updateSongOnNotification();

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showNotification(){
        Intent notify_intent = new Intent(this, MainActivity.class);
        notify_intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pending_intent = PendingIntent.getActivity(this,0, notify_intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationView = new RemoteViews(getPackageName(), R.layout.notification_layout);

        Intent switchIntent = new Intent("SWITCH_EVENT");
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 0, switchIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.notification_play_button, pendingSwitchIntent);

        Intent nextIntent = new Intent("NEXT_EVENT");
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.notification_next_button, pendingNextIntent);

        Intent closeIntent = new Intent("CLOSE_EVENT");
        PendingIntent pendingCloseIntent = PendingIntent.getBroadcast(this, 0, closeIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.notification_close_button, pendingCloseIntent);

        try {
            builder.setContentIntent(pending_intent)
                    .setSmallIcon(R.drawable.ic_stat_notify)
                    .setTicker(currentTrack.getString("track_name"))
                    .setOngoing(true)
                    .setContentTitle("Playing")
                    .setContentText(currentTrack.getString("track_name"));
            setSongNameOnNotification();
        }
        catch (Exception e){
            Log.i(TAG,"showNotification exception");
            e.printStackTrace();
        }

        notification = builder.build();
        notification.contentView = notificationView;

        startForeground(NOTIFY_ID,notification);
    }

    private void updateSongOnNotification(){
        if (notificationView != null){
            setSongNameOnNotification();
            notificationManager.notify(NOTIFY_ID, notification);
        }
        else {
            showNotification();
        }
    }

    private void setSongNameOnNotification(){
        try {
            notificationView.setTextViewText(R.id.Notification_song_name, currentTrack.getString("track_name"));
            notificationView.setTextViewText(R.id.Notification_artist_name, currentTrack.getString("ac"));
            notificationView.setImageViewResource(R.id.notification_play_button, R.drawable.pause_btn);
        }
        catch (Exception e){
            Log.i(TAG,"setSongNameOnNotification exception");
            e.printStackTrace();
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void switchPlay(boolean pause){
        if (player != null){
            if (player.isPlaying()) {
                isPaused = true;
                player.pause();
                updateNotification("pause");
            }
            else if (!pause){
                player.start();
                isPaused = false;
                updateNotification("play");
            }
        }
        else if (checkSpotifyPlayer()){
            if (mPlayerState != null && mPlayerState.playing){
                updateNotification("pause");
                isPaused = true;
                mPlayer.pause();
            }
            else if(!pause){
                updateNotification("play");
                mPlayer.resume();
                isPaused = false;
            }
        }
    }

    private boolean checkSpotifyPlayer(){
        if (mPlayer != null && mPlayer.isLoggedIn()){
            return true;
        }
        return false;
    }

    /**
     * Increment songPosition
     */
    public void nextSong(){
        if (songPosition >= (this.songsList.length() - 1) ){
            songPosition = 0;
        }
        else {
            songPosition++;
        }
    }

    public void stopPlayer(){
        if (player != null){
            player.stop();
            player.release();
            player = null;
        }
        if (threadRunning){
            Log.i(TAG,"LoadAsync Thread interrupted");
            //thread.interrupt();
            task.cancel(true);
            threadRunning = false;
        }
    }

    /**
     * Set songs play list
     * @param songsList
     */
    public void setSongsList(String songsList){
        try {
            this.songsList = new JSONArray(songsList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get current song position
     * @return
     */
    public int getSongPosition(){
        return songPosition;
    }

    /**
     * Check if player is playing any song currently
     * @return
     */
    public boolean isPlaying() {
        if (player != null){
            try {
                return player.isPlaying();
            } catch (Exception e) {
                Log.e(TAG, "isPlaying: " + player.isPlaying() + " Error: " + e.toString());
                return false;
            }
        }

        if (checkSpotifyPlayer()){
            mPlayer.getPlayerState(new PlayerStateCallback() {
                @Override
                public void onPlayerState(PlayerState playerState) {
                    mPlayerState = playerState;
                }
            });
            if (mPlayerState != null) {
                //Log.i(TAG,"play status: " + mPlayerState.playing);
                return mPlayerState.playing;
            }
        }
        return false;
    }

    /**
     * Destroy Spotify player, call this on onDestroy to prevent memory leaks
     */
    public void destroySpotify(){
        mPlayer.shutdown();
        Spotify.destroyPlayer(this);
        mPlayer = null;
    }

    /**
     * Get duration of current song, works only for Spotify Player
     * @return
     */
    public int getDuration(){
        if(isPlaying() && player != null) {
            return player.getDuration();
        }

        if (mPlayerState != null){
            return mPlayerState.durationInMs;
        }

        return 0;
    }

    public int getSongProgress(){
        if(isPlaying())
            return player.getCurrentPosition();
        return 0;
    }

    /**
     * Set current song position
     * @param songPosition
     */
    public void setSongPosition(int songPosition){
        this.songPosition = songPosition;
    }

    private void activateNotificationReceivers(){
        registerReceiver(switchPlayReceiver, new IntentFilter("SWITCH_EVENT"));
        registerReceiver(nextSongReceiver, new IntentFilter("NEXT_EVENT"));
    }

    private void deactivateNotificationReceivers(){
        unregisterReceiver(switchPlayReceiver);
        unregisterReceiver(nextSongReceiver);
    }

    private void updateNotification(String action){
        switch (action) {
            case "pause":
                if(player != null || mPlayer.isLoggedIn()) {
                    if (notificationView != null) {
                        notificationView.setImageViewResource(R.id.notification_play_button, R.drawable.play_btn);
                        notificationManager.notify(NOTIFY_ID, notification);
                    }
                }
                break;
            case "play":
                if(player != null || mPlayer.isLoggedIn()) {
                    //player.start();
                    if (notificationView != null) {
                        notificationView.setImageViewResource(R.id.notification_play_button, R.drawable.pause_btn);
                        notificationManager.notify(NOTIFY_ID, notification);
                    }
                }
                break;
            default:
                break;
        }

    }

    private void sendChangeSongToActivity(){
        Log.i(TAG,"Send broadcast to activity");
        Intent intent = new Intent(MainApplication.CHANGE_SONG);
        intent.putExtra("songPosition", songPosition);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void showToastMessage(final String message){
        Log.i(TAG,"showToastMessage");
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), message,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private BroadcastReceiver songControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"songPosition -> " + intent.getIntExtra("songPosition",0));
            songPosition = intent.getIntExtra("songPosition",0);
            loadSongAsync(false);
        }
    };

    private BroadcastReceiver nextSongReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"nextSongReceiver");
            nextSong();
            //loadSongAsync();
            sendChangeSongToActivity();
        }
    };

    private BroadcastReceiver switchPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "switchPlayReceiver");
            switchPlay(false);
        }
    };

    /**
     * For binding with Activity
     */
    public class MusicServiceBinder extends Binder {
        public MusicService getService() {
            Log.v(TAG, "MusicServiceBinder: getService() called");
            return MusicService.this;
        }
    }

}
