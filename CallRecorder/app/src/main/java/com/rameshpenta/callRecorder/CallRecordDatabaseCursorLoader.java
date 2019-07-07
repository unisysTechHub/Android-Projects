package com.rameshpenta.callRecorder;

import android.app.Activity;
import android.app.Application;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.SimpleCursorTreeAdapter;

/**
 * Created by Sujatha on 12-08-2015.
 */
public class CallRecordDatabaseCursorLoader extends SimpleCursorTreeAdapter {


    public CallRecordDatabaseCursorLoader(Context context, Cursor cursor, int collapsedGroupLayout, int expandedGroupLayout, String[] groupFrom, int[] groupTo, int childLayout, int lastChildLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, collapsedGroupLayout, expandedGroupLayout, groupFrom, groupTo, childLayout, lastChildLayout, childFrom, childTo);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        System.out.println("Ramesh onCreateLoader" );
        CursorLoader cursorLoader=null;

        if(id == Utils.CALLER_TABLE_ByNAME) {
            System.out.println("Ramesh in Caller Table BY name");
            //cursor= context.getContentResolver().query(Utils.CALLER_TABLE_URI, null, null, null, null);
            cursorLoader = new CursorLoader(null, Utils.CALLER_TABLE_URI, null, null, null, null);
        }
        else
        if(id == Utils.CALL_RECORDER_TABLE_ByPhoneNumber)
        {    String phoneNumber= args.getString("PhoneNumber");
            System.out.println("Ramesh Cursor Loadre Phone Number"+ phoneNumber);
            // cursor=context.getContentResolver().query(Utils.CALL_RECORDER_TABLE_BY_PHONE_NUMBER, null,null , new String[]{phoneNumber}, null);
            cursorLoader = new CursorLoader(null, Utils.CALL_RECORDER_TABLE_BY_PHONE_NUMBER, null,null , new String[]{phoneNumber}, null);}
        else
        if (id == Utils.CALL_RECORDER_TABLE_ByDATE)
            cursorLoader = new CursorLoader(null, Utils.CALL_RECORDER_TABLE_BY_DATE_URI, null, null, null, null);
        else
        if(id == Utils.CALL_RECORDER_TABLE_UNIQUE_DATE)
            cursorLoader = new CursorLoader(null, Utils.CALL_RECORDER_TABLE_UNIQUE_DATE_URI, null, null, null, null);
        else
        if(id == Utils.CALLER_TABLE_SEARCH)
            cursorLoader = new CursorLoader(null, Utils.CALLER_TABLE_SEARCH_URI, null, null, null, null);


        return cursorLoader;
    }


    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        System.out.println("Ramesh onLoadFinished");
        cursor=data;
        System.out.println("Ramesh Cursor Null "+ data.getCount());

    }


    public void onLoaderReset(Loader<Cursor> loader) {

    }


    Cursor cursor = null;


    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        return null;
    }
}
