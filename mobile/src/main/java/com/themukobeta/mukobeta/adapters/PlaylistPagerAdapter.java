package com.themukobeta.mukobeta.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.themukobeta.mukobeta.MainActivity;
import com.themukobeta.mukobeta.R;
import com.themukobeta.mukobeta.utils.OnSwipeTouchListener;

import org.json.JSONArray;

import java.util.ArrayList;

public class PlaylistPagerAdapter extends PagerAdapter {
    private static final String TAG = "MukoApp:PlylstPgrAdptr$";

    LayoutInflater inflater;
    Context context;
    JSONArray songsList;
    MainActivity parentActivity;
    ArrayList<View> viewArrayList;

    public PlaylistPagerAdapter(Context context, JSONArray songList, Activity a) {
        this.context = context;
        this.songsList = songList;
        this.parentActivity = (MainActivity) a;
        //data = arraylist;
        viewArrayList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        //Log.i(TAG,"getCount called");
        return songsList.length();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        //Log.i(TAG,"isViewFromObject called");
        return view == ((FrameLayout) object);
    }

    boolean pagerClicked = true;

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.fragment_play, container,
                false);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"pager view clicked");
                pagerClicked = true;
            }
        });

        itemView.setOnTouchListener(new OnSwipeTouchListener(context, this) {
            public void onSwipeBottom() {
                Log.i(TAG,"swipe bottom");
                parentActivity.showQueryPage();
            }
        });

        try {
            ((TextView) itemView.findViewById(R.id.song_name)).setText(songsList.getJSONObject(position).getString("track_name"));
            ((TextView) itemView.findViewById(R.id.artist_name)).setText(songsList.getJSONObject(position).getString("ac"));
        }
        catch (Exception e){
            Log.i(TAG,"setText instantiateItem -> " + e.getMessage());
            e.printStackTrace();
        }

        container.addView(itemView);
        Log.i(TAG,"item added");
        viewArrayList.add(itemView);
        return itemView;

    }

    public void callHandlePagerClick(){
        parentActivity.handlePagerClick();
    }

    public void setPagerClicked(boolean pagerClicked) {
        this.pagerClicked = pagerClicked;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.i(TAG,"destroyItem called");
        ((ViewPager) container).removeView((FrameLayout) object);

    }
}