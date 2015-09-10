package com.themukobeta.mukobeta.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.themukobeta.mukobeta.MainActivity;
import com.themukobeta.mukobeta.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SongDetails.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SongDetails#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SongDetails extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SONG_NAME = "songName";
    private static final String ARTIST_NAME = "artistName";

    // TODO: Rename and change types of parameters
    private String songName;
    private String artistName;

    private MainActivity mainActivity;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param songName Parameter 1.
     * @param artistName Parameter 2.
     * @return A new instance of fragment SongDetails.
     */
    // TODO: Rename and change types and number of parameters
    public static SongDetails newInstance(String songName, String artistName) {
        SongDetails fragment = new SongDetails();
        Bundle args = new Bundle();
        args.putString(SONG_NAME, songName);
        args.putString(ARTIST_NAME, artistName);
        fragment.setArguments(args);
        return fragment;
    }

    public SongDetails() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            songName = getArguments().getString(SONG_NAME);
            artistName = getArguments().getString(ARTIST_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SongDetails Fragment","rootView onClick called");
            }
        };*/

        View rootView = inflater.inflate(R.layout.fragment_song_details, container, false);
        TextView songNameView = (TextView) rootView.findViewById(R.id.songName);
        songNameView.setText(songName);
        //songNameView.setOnClickListener(clickListener);

        TextView artistNameView = (TextView) rootView.findViewById(R.id.artistName);
        artistNameView.setText(artistName);
        //artistNameView.setOnClickListener(clickListener);



        BoxInsetLayout layout = (BoxInsetLayout) rootView.findViewById(R.id.boxRoot);
        //layout.setOnClickListener(clickListener);

        RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.rootLayout);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SongDetails Fragment", "rootView onClick called");
                mainActivity.handleClickOnWatch();
            }
        });

        /*rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SongDetails Fragment","rootView onClick called");
            }
        });*/


        return rootView;
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
        try {
            mListener = (OnFragmentInteractionListener) activity;
            mainActivity = (MainActivity) activity;
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
