package com.covid.scanning;

import android.app.Application;
import com.google.firebase.firestore.FirebaseFirestore;

public class App extends Application {

    public static App minstance;

    @Override
    public void onCreate() {
        super.onCreate();
        minstance = this;
    }

    public static App getInstance() {
        return minstance;
    }

    public FirebaseFirestore getdbInstance() {
        return FirebaseFirestore.getInstance();
    }

    public PreferenceManager getPrefManager() {
        return new PreferenceManager(minstance);
    }
}
