package com.themukobeta.mukobeta.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.util.Log;

import com.themukobeta.mukobeta.fragments.SearchFragment;
import com.themukobeta.mukobeta.fragments.SongDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by abhi on 17/3/15.
 */
public class SongsGridPagerAdapter extends FragmentGridPagerAdapter {
    private static final String TAG = "SongsGridPagerAdapter ->";
    int lastSongColumn = 0;
    boolean changeBackToOld = false;
    private JSONArray songsList;
    public SongsGridPagerAdapter(FragmentManager fm, String songsList) {
        super(fm);
        try {
            this.songsList = new JSONArray(songsList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Fragment getFragment(int row, int col) {
        Log.i(TAG,"getFragment Row: " + row + " Col: " + col);
        try {
            if (row == 0) {
                if (changeBackToOld){
                    col = lastSongColumn;
                    Log.i(TAG,"changeBackToOld Col: " + col);
                }
                JSONObject songObject = songsList.getJSONObject(col);
                //CardFragment fragment = CardFragment.create(songObject.getString("songName"),songObject.getString("artistName"));
                SongDetails fragment = SongDetails.newInstance(songObject.getString("songName"), songObject.getString("artistName"));
                lastSongColumn = col;
                changeBackToOld = false;
                return fragment;
            }
            else if (row == 1){
                Log.i(TAG,"in SearchFragment");
                SearchFragment fragment = SearchFragment.newInstance("test","test");
                changeBackToOld = true;
                return fragment;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return 2;
    }

    @Override
    public int getColumnCount(int i) {
        return (songsList.length() > 0 ? songsList.length() : 1);
    }
}
