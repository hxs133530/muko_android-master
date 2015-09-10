package com.themukobeta.mukobeta.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.themukobeta.mukobeta.MainActivity;
import com.themukobeta.mukobeta.R;
import com.themukobeta.mukobeta.application.MainApplication;
import com.themukobeta.mukobeta.utils.HelperMethods;
import com.themukobeta.mukobeta.utils.OnSwipeTouchListener;
import com.themukobeta.mukobeta.visualizer.SpeechWave;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements RecognitionListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "MukoApp:MainFragment->";

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private boolean recording = false;

    ImageView play_query, record_query;
    EditText query_text;
    SpeechWave speechWave;
    View rootView;


    private MainActivity mainActivity;
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate called");
        initSpeechRecognizer();
    }

    @Override
    public void onDestroy() {
        speech.destroy();
        super.onDestroy();
    }

    private void initSpeechRecognizer(){
        speech = SpeechRecognizer.createSpeechRecognizer(getActivity());
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getActivity().getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    private void searchSongs(){
        Log.i(TAG, "play_query clicked");
        query_text.setVisibility(View.VISIBLE);
        (rootView.findViewById(R.id.splash_text)).setVisibility(View.GONE);
        ((MainActivity)getActivity()).getSongsList(((EditText) rootView.findViewById(R.id.query_text)).getText().toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG,"onCreateView called");
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        play_query = (ImageView) rootView.findViewById(R.id.play_query);
        record_query = (ImageView) rootView.findViewById(R.id.record_query);
        ImageView login_spotify = (ImageView) rootView.findViewById(R.id.login_spotify);

        query_text = (EditText) rootView.findViewById(R.id.query_text);
        query_text.setTextColor(getResources().getColor(R.color.muko_white));
        query_text.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        query_text.getBackground().setColorFilter(getResources().getColor(android.R.color.darker_gray), PorterDuff.Mode.SRC_ATOP);

        speechWave = (SpeechWave) rootView.findViewById(R.id.speech_wave);

        login_spotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainApplication.LOGGED_IN) {
                    mainActivity.openSpotifyLogin();
                }
                else {
                    mainActivity.logoutSpotify();
                }
            }
        });

        record_query.setOnTouchListener(new OnSwipeTouchListener(getActivity(), this) {
            public void onSwipeTop() {
                Log.i(TAG, "swipe top");
                ((MainActivity) getActivity()).showSongPage();
            }
        });

       /* query_text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG,"query_text touch");
                ((MainActivity)getActivity()).stopSongLoading();
                return false;
            }
        });*/

        query_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"query_text clicked");
                query_text.setCursorVisible(true);
                ((MainActivity)getActivity()).stopSongLoading();
            }
        });

        query_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.i(TAG,"OnEditorActionListener: " + actionId);
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    query_text.setCursorVisible(false);
                    HelperMethods.closeKeyboard(getActivity(),query_text.getWindowToken());
                    searchSongs();
                    return true;
                }
                return false;
            }
        });

        return rootView;
    }

    private void showSpeechVisuals(){
        record_query.setVisibility(View.GONE);
        speechWave.setVisibility(View.VISIBLE);
        play_query.setEnabled(false);
        query_text.setEnabled(false);
    }

    private void hideSpeechRecognizer(){
        recording = false;
        play_query.setEnabled(true);
        query_text.setEnabled(true);
        record_query.setVisibility(View.VISIBLE);
        speechWave.setVisibility(View.GONE);
    }

    private void startSpeechRecognition(){
        mainActivity.pausePlayer();
        speech.startListening(recognizerIntent);
    }

    private void stopSpeechRecognition(){
        recording = false;
        speech.cancel();
        play_query.setEnabled(true);
        query_text.setEnabled(true);
    }

    public void handleMicClick(){
        Log.i(TAG, "record_query clicked");
        if (recording){
            stopSpeechRecognition();
            return;
        }
        startSpeechRecognition();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
        Log.i(TAG,"onAttach called");
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setResult(String result)
    {
        hideSpeechRecognizer();
        query_text.setText(result);
        searchSongs();
        //play_query.performClick();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        recording = true;
        showSpeechVisuals();
    }

    @Override
    public void onBeginningOfSpeech()
    {
        Log.i(TAG, "onBeginningOfSpeech");
        speechWave.setYes(-2);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i(TAG, "onRmsChanged value: " + rmsdB);
        speechWave.setYes((int)rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "onEndOfSpeech");
    }

    @Override
    public void onError(int error) {
        String message = getErrorText(error);
        Log.i(TAG, "error: " + message);
        hideSpeechRecognizer();
        HelperMethods.showErrorAlert(message,getActivity());
    }

    @Override
    public void onResults(Bundle results) {
        Log.i("listener", "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
        setResult(matches.get(0));
        Log.i(TAG, "scores: " + Arrays.toString(scores));
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }



    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            /*case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;*/
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            /*case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "Receieved nothing";
                break;*/
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
