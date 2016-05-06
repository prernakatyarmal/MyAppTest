package com.hackensack.umc;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Created by prerana_katyarmal on 2/18/2016.
 */
public class MyApplication extends MultiDexApplication {
        @Override
        protected void attachBaseContext(Context base) {
            super.attachBaseContext(base);
            MultiDex.install(this);}
}

