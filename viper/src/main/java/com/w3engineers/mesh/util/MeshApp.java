package com.w3engineers.mesh.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.w3engineers.ext.strom.App;
import com.w3engineers.ext.strom.BuildConfig;
import com.w3engineers.mesh.application.data.local.db.SharedPref;

import timber.log.Timber;

public class MeshApp extends App {
    private static Context context;
    private static Activity mActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        SharedPref.on(this);


        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                MeshLog.v("activity found oncreate " + activity.getLocalClassName());
//                mActivity = activity;
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
//                MeshLog.v("activity found ondestroyed " + activity.getLocalClassName());
//                mActivity = null;
            }

            /** Unused implementation **/
            @Override
            public void onActivityStarted(Activity activity) {
//                MeshLog.v("activity found onstarted " + activity.getLocalClassName());

//                mActivity = activity;
            }

            @Override
            public void onActivityResumed(Activity activity) {
//                MeshLog.v("activity found onresumed " + activity.getLocalClassName());
                mActivity = activity;
            }
            @Override
            public void onActivityPaused(Activity activity) {
//                MeshLog.v("activity found onpaused " + activity.getLocalClassName());
                mActivity = null;
            }

            @Override
            public void onActivityStopped(Activity activity) {
//                MeshLog.v("activity found onstopped " + activity.getLocalClassName());
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
        });
    }

    public static Context getContext(){
        return context;
    }

    @Override
    protected void plantTimber() {
        Timber.plant(BuildConfig.DEBUG ? getDebugTree() : getReleaseTree());
    }

    private Timber.Tree getDebugTree() {
        return new Timber.DebugTree() {
            //Add line number and method name with tag
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                //The brace will generate clickable link in Logcat window
                //Stability depends on developers comfort level
                return Thread.currentThread().getName() +"-(" + element.getFileName() + ':' + element.getLineNumber() + "):"+element.getMethodName();
            }
        };
    }

    private Timber.Tree getReleaseTree() {
        return new Timber.Tree() {

            @Override
            protected void log(int priority, String tag, String message, Throwable t) { }

            @Override
            protected boolean isLoggable(String tag, int priority) {
                //Do not like to log during release
                return false;
            }
        };
    }


    public static Activity getCurrentActivity() {
        return mActivity;
    }


}
