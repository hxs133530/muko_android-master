package com.themukobeta.mukobeta;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.themukobeta.mukobeta.adapters.SongsGridPagerAdapter;
import com.themukobeta.mukobeta.fragments.SearchFragment;
import com.themukobeta.mukobeta.fragments.SongDetails;

import java.util.List;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SongDetails.OnFragmentInteractionListener, SearchFragment.OnFragmentInteractionListener {

    private static final String TAG = "MukoApp Wear: MainActivity ->";

    private TextView action_text;

    private static final int SPEECH_REQUEST_CODE = 0;

    private static final String SEARCH_QUERY_ON_PHONE = "/searchQuery";
    private static final String CHANGE_SONG_ON_PHONE = "/changeSong";
    private static final String PLAY_PAUSE_ON_PHONE = "/playPause";

    Node mNode; // the connected device to send the message to
    GoogleApiClient mGoogleApiClient;

    private boolean changeSongFromPhone = false;

    public void setChangeSongFromPhone(boolean changeSongFromPhone) {
        this.changeSongFromPhone = changeSongFromPhone;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int status = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getApplicationContext());
        if (status == ConnectionResult.SUCCESS) {
//            Toast.makeText(this,"Compatible Google Play Service Available",Toast.LENGTH_LONG).show();
            Log.v(TAG,"Compatible Google Play Service's found");
        }
        else
            Toast.makeText(this, "Google Play Services Outdated, App will not function", Toast.LENGTH_LONG).show();

        //Connect the GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        MobileInterface mobileInterface = new MobileInterface(this);


        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                action_text = (TextView) findViewById(R.id.action_text);
                RelativeLayout mic_icon = (RelativeLayout) findViewById(R.id.mic_img);
                mic_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG,"record clicked");
                        displaySpeechRecognizer();
                    }
                });
            }
        });


    }

    public void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    private void resolveNode() {
        Log.i(TAG,"Connect to mobile");
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    mNode = node;
                }
            }
        });
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected");
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            action_text.setText("Fetching songs for " + spokenText);
            Log.i(TAG,"Result: " + spokenText);
            sendMessage(spokenText);
        }
    }

    /**
     * Send message to mobile handheld
     */
    private void sendMessage(String query) {
        Log.i(TAG,"Send message to mobile");
        if (mNode != null && mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
            Log.i(TAG,"Send message query: " + query);
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), SEARCH_QUERY_ON_PHONE, query.getBytes()).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }

    }

    private void sendSongChangeMessage(int songPosition) {
        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG,"changeSongPosition done");
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(CHANGE_SONG_ON_PHONE);

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

    public void handleSongsList(String songs){
        final GridViewPager mGridPager = (GridViewPager) findViewById(R.id.songsPager);
        mGridPager.setAdapter(new SongsGridPagerAdapter(getFragmentManager(), songs));
        mGridPager.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, int i2, float v, float v2, int i3, int i4) {

            }

            @Override
            public void onPageSelected(int row, int col) {
                Log.i(TAG,"onPageSelected : Row ->" + row + " Col -> " + col );

                if (row == 0 && !changeSongFromPhone) {
                    sendSongChangeMessage(col);
                }
                setChangeSongFromPhone(false);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        mGridPager.setVisibility(View.VISIBLE);
        //mGridPager.setCurrentItem(1,1);
        (findViewById(R.id.mic_img)).setVisibility(View.GONE);
        action_text.setVisibility(View.GONE);
    }

    public void handleClickOnWatch(){
        sendPlayPauseMessage();
    }

    private void sendPlayPauseMessage(){
        if (mNode != null && mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
            Log.i(TAG,"Play/Pause message");
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), PLAY_PAUSE_ON_PHONE, "play/pause".getBytes()).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void setPage(int position){
        setChangeSongFromPhone(true);
        GridViewPager mGridPager = (GridViewPager) findViewById(R.id.songsPager);
        mGridPager.setCurrentItem(1,position);
    }
}
