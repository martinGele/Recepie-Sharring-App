package com.martinrecipe.italian;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Gele on 29.6.2017.
 */

public class CustomFont extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("CaviarDreams.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
