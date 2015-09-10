package com.themukobeta.mukobeta.utils;

import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.themukobeta.mukobeta.adapters.PlaylistPagerAdapter;
import com.themukobeta.mukobeta.fragments.MainFragment;

public class OnSwipeTouchListener implements OnTouchListener {

    private final GestureDetector gestureDetector;
    PlaylistPagerAdapter playlistPagerAdapter;
    MainFragment mainFragment;

    public OnSwipeTouchListener (Context ctx, PlaylistPagerAdapter pl){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        playlistPagerAdapter = pl;
    }

    public OnSwipeTouchListener(Context ctx, Fragment fragment){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        mainFragment = (MainFragment) fragment;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (playlistPagerAdapter != null) {
            playlistPagerAdapter.setPagerClicked(false);
        }
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            Log.i("listener","ondown");
            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i("listener","single tap up");
            if (playlistPagerAdapter != null) {
                playlistPagerAdapter.callHandlePagerClick();
            }
            else if (mainFragment != null){
                mainFragment.handleMicClick();
            }
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    result = true;
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {

    }
}