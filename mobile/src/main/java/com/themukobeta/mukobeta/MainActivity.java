package com.themukobeta.mukobeta;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;
import com.themukobeta.mukobeta.adapters.PlaylistPagerAdapter;
import com.themukobeta.mukobeta.application.MainApplication;
import com.themukobeta.mukobeta.fragments.MainFragment;
import com.themukobeta.mukobeta.fragments.SongsList;
import com.themukobeta.mukobeta.services.MusicService;
import com.themukobeta.mukobeta.utils.AsyncRequest;
import com.themukobeta.mukobeta.utils.HelperMethods;
import com.themukobeta.mukobeta.utils.ResizeAnimation;
import com.themukobeta.mukobeta.watch_interface.WatchInterface;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends Activity implements MainFragment.OnFragmentInteractionListener, AsyncRequest.OnAsyncRequestComplete, SongsList.OnFragmentInteractionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ConnectionStateCallback, PlayerNotificationCallback {

    private static final String TAG = "MukoApp:MainActivity->";
    private static final String SONGS_LIST = "/songslist";
    private static final String CHANGE_SONG_ON_WEAR = "/changeSongOnWear";

    Intent musicServiceIntent;
    private MusicService musicService;

    private ServiceConnection serviceConnection = new MusicServiceConnection();

    //gesture detector
    private GestureDetectorCompat gDetect;
    private BroadcastReceiver closeAppReceiver;

    ScheduledExecutorService myScheduledExecutorService;
    ScheduledFuture test;


    private boolean pagerInitialized = false;
    private boolean changeSongFromWear = false;

    /**
     * Height of screen for water level progress
     */
    private int rootViewHeight;

    /**
     * Number of pixels to increase for song progress
     */
    private float ratePixels;

    /**
     * Duration of the song
     */
    private int musicDuration;

    private boolean running = false;

    private float progressBackground = 0;

    private boolean playlist_visible = false;

    private boolean pageSwiped = false;

    GoogleApiClient mGoogleApiClient;

    public static Handler monitorHandler;


    RelativeLayout relativeLayout, playlist_layout, footer;
    //LineChartView songLoader;
    ImageView songLoader;

    AtomicBoolean progress = new AtomicBoolean();
    PlaylistPagerAdapter songsListAdapter;
    WatchInterface watchInterface;

    //Spotify Details
    private Player mPlayer;
    private static final String CLIENT_ID = "fdbbb7e9ad184fc8b6b67126e2f01530";
    private static final String REDIRECT_URI = "muko://callback";
    private static final int REQUEST_CODE = 1337;
    private String accessToken = "";

    /**
     * Check if user is logged in
     */
    private boolean userLoggedIn = false;


    /**
     * Set screen height
     */
    private void updateSizeInfo() {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rootView);
        rootViewHeight = relativeLayout.getHeight();
    }

    /**
     * Initialize Google API Client for SpeechRecognition
     */
    private void initializeGoogleAPIClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Retrieve songs list from the Muko server for a query
     * @param query
     */
    public void getSongsList(String query){
        Log.i(TAG,"query -> " + query );
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.accumulate("userQuery", query);
        }
        catch (Exception e){
            Log.e(TAG,"getSongsList -> " + e.getMessage());
            e.printStackTrace();
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("q",query));
        AsyncRequest getSongsList = new AsyncRequest(this,"GET", params);
        getSongsList.execute(MainApplication.MUKO_API);

    }

    /**
     * Handle response from getSongsList API request
     * @param response
     */
    @Override
    public void asyncResponse(String response) {
        final JSONArray songsList;
        try{
            final ImageView left_arrow = (ImageView) findViewById(R.id.left_arrow);
            final ImageView right_arrow = (ImageView) findViewById(R.id.right_arrow);
            songsList = new JSONArray(response);
            musicService.setSongsList(response);
            musicService.setSongPosition(0);

            //set viewpager adapter
            songsListAdapter = new PlaylistPagerAdapter(this,songsList, this);
            ViewPager pager = (ViewPager) findViewById(R.id.playlist_pager);
            pager.setAdapter(songsListAdapter);
            pagerInitialized = true;

            //song water level progress indicator
            switchExecutor();
            running = false;
            playlist_visible = false;
            resetProgress();

            //load song
           if (mPlayer != null && mPlayer.isLoggedIn()){
                musicService.playSpotifyPlayer();
            }
            else {
                musicService.loadSongAsync(false);
            }

             //songLoader.setAnimation(true);
            setSongDetails(musicService.getCurrentTrack());
            runProgressCheck();


            pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                public void onPageScrollStateChanged(int state) {
                    //Log.i(TAG,"onPageScrollStateChanged called -> " + state);
                    int position = musicService.getSongPosition();
                    if (state == 1){
                        if (position > 0 && left_arrow.getVisibility() == View.GONE) {
                            HelperMethods.showView(left_arrow);
                        } else if (position <= 0 && left_arrow.getVisibility() == View.VISIBLE) {
                            HelperMethods.hideView(left_arrow);
                        }

                        if (position >= (songsList.length() - 1) && right_arrow.getVisibility() == View.VISIBLE) {
                            HelperMethods.hideView(right_arrow);
                        }
                        else if (right_arrow.getVisibility() == View.GONE && !(position >= (songsList.length() - 1))){
                            HelperMethods.showView(right_arrow);
                        }
                    }
                    else{
                        HelperMethods.hideView(left_arrow);
                        HelperMethods.hideView(right_arrow);
                    }
                }
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    //Log.i(TAG,"onPageScrolled called -> " + position);
                }

                public void onPageSelected(int position) {
                    //Log.i(TAG,"onPageSelected called -> " + songsList.length());

                    if (running){
                        switchExecutor();
                    }

                    musicService.switchPlay(true);
                    musicService.setSongPosition(position);
                    pageSwiped = true;
                    //songLoader.setAnimation(true);
                    //musicService.loadSongAsync(true);
                    playSong();
                    setSongDetails(musicService.getCurrentTrack());

                    resetProgress();
                    switchExecutor();

                    if (!changeSongFromWear){
                        sendSongChangeMessage(position);
                    }

                    setChangeSongOnWear(false);

                }
            });

            sendSongsList(response);
            Log.i(TAG,"AsyncResponse -> " + response );
        }
        catch (Exception e){
            Log.e(TAG,"getSongsList response -> " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send songs list to Android wear
     * @param songsList
     */
    private void sendSongsList(String songsList){
        Log.i(TAG,"sendSongsList");
        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG,"sendSongsList done");
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(SONGS_LIST);

            // Add data to the request
            putDataMapRequest.getDataMap().putString("songsList", songsList);
            PutDataRequest request = putDataMapRequest.asPutDataRequest();

            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Log.d(TAG, "putDataItem status: " + dataItemResult.getStatus().toString());
                        }
                    });
        }
    }

    /**
     * Send song change message to Android wear
     * @param songPosition
     */
    private void sendSongChangeMessage(int songPosition){
        Log.i(TAG,"sendSongChangeMessage");
        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG,"sendSongsList done");
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(CHANGE_SONG_ON_WEAR);

            // Add data to the request
            putDataMapRequest.getDataMap().putInt("songPosition", songPosition);
            PutDataRequest request = putDataMapRequest.asPutDataRequest();

            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Log.d(TAG, "putDataItem status: " + dataItemResult.getStatus().toString());
                        }
                    });
        }
    }


    //--------------------------------Activity Lifecycle callbacks--------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate called");
        setContentView(R.layout.activity_main);
        gDetect = new GestureDetectorCompat(this, new GestureListener());

        footer = (RelativeLayout)findViewById(R.id.muko_play_list_activity_relativelayout_footer);
        relativeLayout = (RelativeLayout) findViewById(R.id.progress_layout);
        //songLoader = (LineChartView) findViewById(R.id.wave);
        songLoader = (ImageView) findViewById(R.id.wave);

        playlist_layout = (RelativeLayout) findViewById(R.id.playlist);
        (findViewById(R.id.song_list)).setVisibility(View.GONE);
        activateReceiver();

        EditText ed = (EditText)findViewById(R.id.query_text);
        if (ed != null) {
            ed.setHorizontallyScrolling(false);
            //ed.setLines(3);
            ed.setSingleLine(false);
        }

        watchInterface = new WatchInterface(this);

        initializeGoogleAPIClient();

        if(checkConnectivity()){
            checkSpotifyAccess();
        }
        else {
            Log.i(TAG,"user is offline");
            HelperMethods.showErrorAlert(getResources().getString(R.string.offline_user), this);
        }

        myScheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        progress.set(true);
        Log.i(TAG,"onPause called");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(TAG,"onResume called");
        musicServiceIntent = new Intent(this, MusicService.class);
        bindService(musicServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        progress.set(false);
    }

    @Override
    protected void onDestroy(){
        Log.i(TAG,"onDestroy called");
        stopService(musicServiceIntent);
        myScheduledExecutorService.shutdownNow();
        deactivateReceivers();
        unbindService(serviceConnection);
        watchInterface.deactivateReceivers();
        Spotify.destroyPlayer(mPlayer);
        super.onDestroy();
    }


    //--------------------------------Activity callbacks--------------------------------
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i(TAG, "onWindowFocusChanged called");
        updateSizeInfo();
    }

    /**
     * Handle response from Spotify Authentication
     * @param requestCode - check for the same request code
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Log.i(TAG,"accessToken: " + response.getAccessToken());
                handleSpotifyLogin(response.getAccessToken());

                SharedPreferences settings = getSharedPreferences(MainApplication.SPOTIFY_INFO, Context.MODE_PRIVATE);
                SharedPreferences.Editor settingsEditor = settings.edit();
                settingsEditor.putString(MainApplication.SPOTIFY_ACCESS_TOKEN, response.getAccessToken());
                settingsEditor.apply();
            }
        }
    }

    /**
     * Gesture recognition callback
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){
        return this.gDetect.onTouchEvent(event);
    }

    /**
     * Handle back button, send application to back on back button press
     */
    @Override
    public void onBackPressed() {
        if (musicService.isPlaying()){
            moveTaskToBack(true);
        }
        else {
            finish();
        }
    }

    /**
     * Handle orientation change
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            super.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    //--------------------------------Spotify Auth Methods--------------------------------

    /**
     * Open Spotify Login modal
     */
    public void openSpotifyLogin(){
        if (checkConnectivity()) {
            if (mPlayer != null) {
                musicService.destroySpotify();
                Spotify.destroyPlayer(this);
                try {
                    mPlayer.awaitTermination(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mPlayer = null;
            }

            Log.i(TAG,"open ref count: " + Spotify.getReferenceCount());
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                    .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"});

            final AuthenticationRequest request = builder.build();
            AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        }
    }

    /**
     * Check if user is already logged in Spotify, when app starts
     */
    private void checkSpotifyAccess(){
        SharedPreferences settings = getSharedPreferences(MainApplication.SPOTIFY_INFO, Context.MODE_PRIVATE);
        accessToken = settings.getString(MainApplication.SPOTIFY_ACCESS_TOKEN,"");
        userLoggedIn = settings.getBoolean(MainApplication.LOGGED_IN_USER,false);

        Log.i(TAG,"on create accessToken: " + accessToken);

    }

    /**
     * Save access token to SharedPreferences on successful login and setting Spotify player in MusicService
     * @param accessToken
     */
    private void handleSpotifyLogin(String accessToken){
        Log.i(TAG,"handleSpotifyLogin");
        TextView login_status = (TextView) findViewById(R.id.login_status);
        login_status.setText("Signing in...");
        HelperMethods.showView(login_status);

        Config playerConfig = new Config(this, accessToken, CLIENT_ID);
        playerConfig.useCache(false);
        mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {

                if (mPlayer != null) {
                    Log.i(TAG,"onInitialized Spotify Player");
                    mPlayer.setConnectivityStatus(HelperMethods.getNetworkConnectivity(MainActivity.this));
                    mPlayer.addPlayerNotificationCallback(MainActivity.this);
                    mPlayer.addConnectionStateCallback(MainActivity.this);
                    //mPlayer.play("spotify:track:1mea3bSkSGXuIRvnydlB5b");
                    musicService.setmPlayer(mPlayer);
                }
                else {
                    mPlayer = player;
                    mPlayer.setConnectivityStatus(HelperMethods.getNetworkConnectivity(MainActivity.this));
                    mPlayer.addPlayerNotificationCallback(MainActivity.this);
                    mPlayer.addConnectionStateCallback(MainActivity.this);
                    //mPlayer.play("spotify:track:1mea3bSkSGXuIRvnydlB5b");
                    musicService.setmPlayer(mPlayer);
                    Log.i(TAG,"null mplayer");
                }
            }
            @Override
            public void onError(Throwable error) {
                Log.e(TAG,"onActivityResult error");
            }
        });
    }

    /**
     * Logout Spotify and remove access token from SharedPreferences
     */
    public void logoutSpotify(){
        Log.i(TAG,"ref count: " + Spotify.getReferenceCount());
        mPlayer.pause();
        mPlayer.logout();

        SharedPreferences settings = getSharedPreferences(MainApplication.SPOTIFY_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putString(MainApplication.SPOTIFY_ACCESS_TOKEN,"");
        settingsEditor.putBoolean(MainApplication.LOGGED_IN_USER,false);
        settingsEditor.apply();
        Log.i(TAG,"ref count: " + Spotify.getReferenceCount());
    }

    //--------------------------------Wear control--------------------------------
    public void handlePlayPauseFromWear(){
        musicService.switchPlay(false);
    }

    public void setChangeSongOnWear(boolean a){
        this.changeSongFromWear = a;
    }

    public void handleQueryFromWear(String query){
        getSongsList(query);
        ((EditText)findViewById(R.id.query_text)).setText(query);
    }


    //--------------------------------Song progress indicator control--------------------------------
    private void switchExecutor(){
        progress.set(false);
        if (!running){
            Log.i(TAG, "Start Executor Service");
            runProgressCheck();
        }
        else if (musicService.isPlaying()){
            Log.i(TAG, "Stopped Executor Service");
            test.cancel(false);
            running = false;
        }
    }

    /**
     * Handle song progress water level
     */
    private void runProgressCheck(){
        Log.i(TAG,"start monitor handler");
        running  = true;
        test = myScheduledExecutorService.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        monitorHandler.sendMessage(monitorHandler.obtainMessage());
                    }
                },
                200, //initialDelay
                500, //delay
                TimeUnit.MILLISECONDS);


        try{
            monitorHandler = new Handler(){

                @Override
                public void handleMessage(Message msg) {
                    if((musicService != null && musicService.isPlaying())){
                        if (!playlist_visible) {
                            Log.i(TAG,"Playlist show");
                            animateSlideUp(playlist_layout, findViewById(R.id.search_fragment));
                            showProgress();
                            showWave();
                            HelperMethods.hideView(footer);
                            HelperMethods.closeKeyboard(getApplicationContext(),(findViewById(R.id.query_text)).getWindowToken());
                            playlist_visible = true;
                        }

                        musicDuration = 0;
                        musicDuration = musicService.getDuration();

                        if (musicDuration == 0 )
                            musicDuration = 30000;



                        if (!progress.get()) {
                            /*if (progressBackground > 1){
                                ratePixels = ratePixels * ((progressBackground/2.5f) - 1);
                            }
                            Log.i(""," musicDuration: " + musicDuration + " progress: " + ratePixels);

                            progressBackground = 0;
*/
                            final int height = relativeLayout.getHeight();
                            if (mPlayer != null && mPlayer.isLoggedIn()) {
                                ratePixels = 0;


                                mPlayer.getPlayerState(new PlayerStateCallback() {
                                    @Override
                                    public void onPlayerState(PlayerState playerState) {
                                        if (playerState.positionInMs != 0) {
                                            ratePixels = (rootViewHeight / (musicDuration / 500)) + 0.4f;
                                            Log.i("", " rate pixels before: "  + ratePixels);

                                            ratePixels = ratePixels * (playerState.positionInMs / 500);
                                            Log.i("", " musicDuration: " + playerState.positionInMs / 500 + " progress: " + ratePixels);

                                            ResizeAnimation resizeAnimation = new ResizeAnimation(relativeLayout, height, ratePixels);
                                            resizeAnimation.setAnimationListener(new Animation.AnimationListener() {
                                                @Override
                                                public void onAnimationStart(Animation animation) {
                                                    progress.set(true);
                                                }

                                                @Override
                                                public void onAnimationEnd(Animation animation) {
                                                    progress.set(false);
                                                }

                                                @Override
                                                public void onAnimationRepeat(Animation animation) {

                                                }
                                            });
                                            relativeLayout.startAnimation(resizeAnimation);
                                        }
                                    }
                                });
                            }
                            else {
                                ratePixels = (rootViewHeight / (musicDuration / 500)) + 0.4f;
                                if (progressBackground > 1){
                                    ratePixels = ratePixels * ((progressBackground/2.5f) - 1);
                                }
                                Log.i(""," musicDuration: " + musicDuration + " progress: " + ratePixels);

                                progressBackground = 0;

                                ResizeAnimation resizeAnimation = new ResizeAnimation(relativeLayout, height, height + ratePixels);
                                resizeAnimation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        progress.set(true);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        progress.set(false);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                relativeLayout.startAnimation(resizeAnimation);
                            }

                        }
                        else{
                            progressBackground++;
                        }

                    }
                }
            };
        }
        catch(Exception e){
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }


    //--------------------------------UI control methods--------------------------------
    private void showProgress(){
        (findViewById(R.id.progress_layout)).setAlpha(0.5f);
    }

    private void hideProgress(){
        (findViewById(R.id.progress_layout)).setAlpha(0);
    }

    public void showQueryPage(){
        animateSlideDown(findViewById(R.id.search_fragment), playlist_layout);

        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                hideWave();
                hideProgress();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                HelperMethods.showView(footer);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        footer.startAnimation(fadeIn);
    }

    public void showSongPage(){
        if(pagerInitialized) {
            animateSlideUp(playlist_layout, findViewById(R.id.search_fragment));
            playlist_visible = true;
            Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

            footer.startAnimation(fadeOut);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    showWave();
                    showProgress();
                    HelperMethods.hideView(footer);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {


                }
            });
        }
    }

    /**
     * Generic method for sliding in and up animation
     * @param in
     * @param out
     */
    private void animateSlideUp(final View in, final View out){
        Animation slideOutUp = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);
        out.startAnimation(slideOutUp);
        slideOutUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Animation slideInBottom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom);
                in.startAnimation(slideInBottom);
                slideInBottom.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        in.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                out.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    /**
     * Generic method sliding out and down animation
     * @param in
     * @param out
     */
    private void animateSlideDown(final View in, final View out){
        Animation slideOutBottom = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
        out.startAnimation(slideOutBottom);

        slideOutBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Animation slideInBottom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_top);
                in.startAnimation(slideInBottom);
                slideInBottom.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        in.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                out.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    /**
     * Reset water level progress
     */
    private void resetProgress(){
        ResizeAnimation resizeAnimation = new ResizeAnimation(relativeLayout,relativeLayout.getHeight(),1);
        relativeLayout.startAnimation(resizeAnimation);
        resizeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.i(TAG,"start height: " + relativeLayout.getHeight());
                progress.set(true);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.i(TAG,"end height: " + relativeLayout.getHeight());
                progress.set(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void showWave(){
        /*if (!getViewVisibility(footer)){*/songLoader.setVisibility(View.VISIBLE);
    }

    private void hideWave(){
        songLoader.setVisibility(View.INVISIBLE);
    }


    //--------------------------------Spotify callback methods--------------------------------
    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLoggedIn() {
        Log.i(TAG,"onLoggedIn called");
        TextView login_status = (TextView) findViewById(R.id.login_status);
        login_status.setText("Signed in");
        HelperMethods.showView(login_status);
        MainApplication.LOGGED_IN = true;

        SharedPreferences settings = getSharedPreferences(MainApplication.SPOTIFY_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putBoolean(MainApplication.LOGGED_IN_USER,true);
        settingsEditor.apply();
    }

    @Override
    public void onLoggedOut() {
        Log.i(TAG,"onLoggedOut called");
        MainApplication.LOGGED_IN = false;
        TextView login_status = (TextView) findViewById(R.id.login_status);
        login_status.setText("Logged out");
        HelperMethods.showView(login_status);

        /*SharedPreferences settings = getSharedPreferences(MainApplication.SPOTIFY_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putString(MainApplication.SPOTIFY_ACCESS_TOKEN,"");
        settingsEditor.putBoolean(MainApplication.LOGGED_IN_USER,false);
        settingsEditor.apply();*/
    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        if (eventType.compareTo(EventType.TRACK_END) == 0 && !pageSwiped){
            musicService.nextSong();
            setPage(musicService.getSongPosition());
            Log.i(TAG,"Track end: " +running);
        }
        else if (pageSwiped && eventType.compareTo(EventType.TRACK_END) == 0){
            pageSwiped = false;
        }

        Log.i(TAG,"Track event " + eventType.compareTo(EventType.TRACK_END) + " event name: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }



    //--------------------------------Music Service methods--------------------------------

    /**
     * Play current song
     */
    private void playSong(){
        if (mPlayer != null && mPlayer.isLoggedIn()){
            musicService.playSpotifyPlayer();
        }
        else {
            musicService.loadSongAsync(false);
        }
    }

    /**
     * Pause song
     */
    public void pausePlayer(){
        if (test != null) {
            test.cancel(true);
        }
        musicService.switchPlay(true);
    }

    public void stopSongLoading(){
        Log.i(TAG,"running: " + running);
        if (!running && !musicService.isPlaying() && !musicService.isPaused()){
            test.cancel(true);
            running = false;
            musicService.stopPlayer();
        }
    }

    /**
     * Handle play/pause on click on song view
     */
    public void handlePagerClick(){
        switchExecutor();
        musicService.switchPlay(false);
    }

    /**
     * Set current song
     * @param position - position in JSONArray
     */
    public void setPage(int position){
        ViewPager pager = (ViewPager) findViewById(R.id.playlist_pager);
        pager.setCurrentItem(position,true);
    }


    //--------------------------------General methods--------------------------------
    public void checkTerminated(){
        if (mPlayer != null && mPlayer.isTerminated()){
            Log.i(TAG,"is terminated");
        }
    }

    private boolean checkConnectivity(){
        if(HelperMethods.getNetworkConnectivity(this).equals(Connectivity.OFFLINE) ){
            return false;
        }
        return true;
    }

    private void setSongDetails(JSONObject track){
        TextView songName = (TextView) findViewById(R.id.muko_play_list_activity_textview_songname);

        try {
            songName.setText(track.getString("track_name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void activateReceiver(){
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                new IntentFilter(MainApplication.CHANGE_SONG));

        registerReceiver(closeAppReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        finish();
                    }
                },
                new IntentFilter("CLOSE_EVENT"));
    }

    private void deactivateReceivers(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        unregisterReceiver(closeAppReceiver);
    }

    //--------------------------------Private classes--------------------------------

    /**
     * For binding MusicService with MainActivity
     */
    private final class MusicServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            Log.d(TAG,"MusicServiceConnection: Service connected");
            musicService = ((MusicService.MusicServiceBinder) iBinder).getService();
            startService(musicServiceIntent);
            if (accessToken != "" && userLoggedIn){
                TextView login_status = (TextView) findViewById(R.id.login_status);
                login_status.setText("Signing in...");
                HelperMethods.showView(login_status);
                Config playerConfig = new Config(getApplicationContext(), accessToken, CLIENT_ID);
                playerConfig.useCache(false);
                mPlayer = Player.create(playerConfig,new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        Log.i(TAG,"onInitialized called already in");
                        mPlayer.setConnectivityStatus(HelperMethods.getNetworkConnectivity(MainActivity.this));
                        mPlayer.addPlayerNotificationCallback(MainActivity.this);
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        musicService.setmPlayer(mPlayer);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG,"onInitialized called already in");
                    }
                });
                mPlayer.login(accessToken);

            }
        }
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG,"MusicServiceConnection: Service disconnected");
            musicService = null;
        }
    }

    /**
     * Change song listener from Android Wear
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"CHANGESONG received");
            setPage(intent.getIntExtra("songPosition",0));
        }
    };

    /**
     * Custom Gesture Detection for swipe up/down
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private float flingMin = 30;
        private float velocityMin = 30;

        /*//user will move forward through messages on fling up or left
        boolean forward = false;
        //user will move backward through messages on fling down or right
        boolean backward = false;*/


        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i(TAG,"onSingleTapUp");
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,float velocityX, float velocityY) {
            //determine what happens on fling events
            //calculate the change in X position within the fling gesture
            //float horizontalDiff = event2.getX() - event1.getX();
            //calculate the change in Y position within the fling gesture
            if (event1 != null && event2 != null) {
                float verticalDiff = event2.getY() - event1.getY();
                Log.i("Points", "ev1: " + event1.getY() + " ev2: " + event2.getY());


                //float absHDiff = Math.abs(horizontalDiff);
                float absVDiff = Math.abs(verticalDiff);
                //float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(velocityY);

            /*if(absHDiff>absVDiff && absHDiff>flingMin && absVelocityX>velocityMin){
                //move forward or backward
                if(horizontalDiff>0) backward=true;
                else forward=true;
            }*/
                if (absVDiff > flingMin && absVelocityY > velocityMin && pagerInitialized) {
                    if (event1.getY() < event2.getY()) {
                        Log.i("Gesture", "Swipe Down");
                        if (HelperMethods.getViewVisibility(playlist_layout)) {
                            showQueryPage();
                        }


                    /*else if (getViewVisibility(findViewById(R.id.song_list))){
                        animateSlideDown(playlist_layout,findViewById(R.id.song_list));
                    }*/
                    /*(findViewById(R.id.right_arrow)).setVisibility(View.GONE);
                    (findViewById(R.id.left_arrow)).setVisibility(View.GONE);*/
                    } else if (event1.getY() > event2.getY()) {
                        Log.i("Gesture", "Swipe Up");

                        if (HelperMethods.getViewVisibility(findViewById(R.id.search_fragment))) {
                            showSongPage();
                        }
                    /*else if (getViewVisibility(playlist_layout)){
                        animateSlideUp( findViewById(R.id.song_list), playlist_layout);
                    }*/



                    /*(findViewById(R.id.right_arrow)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.search_fragment)).setVisibility(View.VISIBLE);*/
                    }
            /*if(verticalDiff>0) backward=true;
            else forward=true;*/
                }
            }
            return true;


        }

    }
}


