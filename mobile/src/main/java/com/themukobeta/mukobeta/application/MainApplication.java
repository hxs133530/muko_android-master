package com.themukobeta.mukobeta.application;

import android.app.Application;

/**
 * Created by abhi on 9/3/15.
 */
public class MainApplication extends Application {
    public static final String appKey = "rw7q9pmxxpqeubz4bsz2xfbc";
    public static final String appSecret = "hHXgxbgNXu";

    public static final String PREF_ACCESSTOKEN = "prefs.accesstoken";
    public static final String PREF_ACCESSTOKENSECRET = "prefs.accesstokensecret";

    public static final String RDIO_ACCESS = "RDIOACCESSINFO";

    //public static final String MUKO_API = "http://ec2-54-68-231-15.us-west-2.compute.amazonaws.com/queryinjson";
    //public static final String MUKO_API = "http://54.186.26.193/api/v1/songs";
    public static final String MUKO_API = "http://themuko.com/api/v1/songs";

    public static final String SPOTIFY_INFO = "SPOTIFY_INFO";
    public static final String SPOTIFY_ACCESS_TOKEN = "SPOTIFY_ACCESS_TOKEN";
    public static final String LOGGED_IN_USER = "LOGGED_IN_USER";


    public static final String SERVICE_BROADCAST = "MSGFROMSERVICE";
    public static final String CHANGE_SONG = "CHANGESONG";

    public static final String BROADCAST_QUERY_FROM_WEAR = "GET_NEW_SONG_LIST";
    public static final String SWITCH_PLAY = "SWITCH_PLAY";

    public static boolean LOGGED_IN = false;
}
