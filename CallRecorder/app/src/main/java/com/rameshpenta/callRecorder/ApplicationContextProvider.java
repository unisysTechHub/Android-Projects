package com.rameshpenta.callRecorder;
import android.app.Application;
import android.content.Context;

/**
 * Created by Sujatha on 16-05-2015.
 */
public class ApplicationContextProvider extends Application {

    private static Context sContext;



    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();

    }

    /**
     * Returns the application context
     *
     * @return application context
     */
    public static Context getContext() {

               return sContext;
    }

}
