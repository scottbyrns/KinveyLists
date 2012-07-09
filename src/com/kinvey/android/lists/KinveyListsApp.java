package com.kinvey.android.lists;

import android.app.Application;

import com.kinvey.KCSClient;
import com.kinvey.KinveySettings;

/**
 * Store global state. In this case, the single instance of KCS.
 * 
 */
public class KinveyListsApp extends Application {

    private KCSClient service;

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    private void initialize() {
        service = KCSClient.getInstance(getApplicationContext(),KinveySettings.loadFromProperties(getApplicationContext()));
    }

    public KCSClient getKinveyService() {
        return service;
    }

}
