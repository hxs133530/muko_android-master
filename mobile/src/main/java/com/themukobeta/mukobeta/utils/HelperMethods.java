package com.themukobeta.mukobeta.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.spotify.sdk.android.player.Connectivity;

/**
 * Created by abhi on 14/4/15.
 */
public class HelperMethods {

    public static Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }

    public static boolean getViewVisibility(View view){
        if (view.getVisibility() == View.VISIBLE){
            return true;
        }
        else {
            return false;
        }
    }

    public static void showView(View view){
        view.setVisibility(View.VISIBLE);
    }

    public static void hideView(View view){
        view.setVisibility(View.GONE);
    }

    public static void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }

    public static void showErrorAlert(String message, Activity activity){
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

}
