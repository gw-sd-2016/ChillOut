package com.example.grayapps.contextaware;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by AGray on 3/30/16.
 */
public class BreatheApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "SelfAware", "PrimaryTestingKey");
    }
}
