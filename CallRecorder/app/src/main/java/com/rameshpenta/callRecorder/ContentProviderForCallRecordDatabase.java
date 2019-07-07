package com.rameshpenta.callRecorder;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class ContentProviderForCallRecordDatabase extends ContentProvider {
    public ContentProviderForCallRecordDatabase() {
    }
    private static UriMatcher uriMatcher = new UriMatcher(-1);
    static {
            System.out.println("Ramesh addURI");
        uriMatcher.addURI("callRecorderApp.database","CALLERTable",1);
        uriMatcher.addURI("callRecorderApp.database","CALLRECORDERTableByPhoneNumber",2);

         uriMatcher.addURI("callRecorderApp.database","CALLRECORDERTableByDate",3);
        uriMatcher.addURI("callRecorderApp.database","CALLRECORDERTableUniqueDate",4);
        uriMatcher.addURI("callRecorderApp.database","CALLERTableSearch",5);

            }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        System.out.println("Ramesh Content Prvoider onCreate");

        // TODO: Implement this to initialize your content provider on startup.
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor=null;
            System.out.println("Ramesh Content Provider uri "+uriMatcher.match(uri) );
            CallRecordDataBase callRecordDataBase= new CallRecordDataBase(getContext().getApplicationContext());
            callRecordDataBase.openDatabase();
        switch (uriMatcher.match(uri))
        {
            case 1:
                System.out.println("Ramesh URI Matcher caller table sorty by Name");
                 cursor=callRecordDataBase.getCallerInfoSortByName();
                break;
            case 2:
                System.out.println("Ramesh URI Matcher callRECORDER table by Phoe NUmber");
                cursor=callRecordDataBase.getCallerRecords(selectionArgs[0]);
                break;
            case 3:
                System.out.println("Ramesh URI Matcher getCallerRecords byDate");
                cursor=callRecordDataBase.getCallerRecordsByDate(selectionArgs[0]);
                break;

            case 4:
                System.out.println("Ramesh URI Matcher get callerRecords by unique date ");
                cursor=callRecordDataBase.getCallerRecordsUniqueDate();
                break;
            case 5:
                System.out.println("URI Matcher callerTable search");
                cursor=callRecordDataBase.callerTableSearch(selectionArgs[0]);
                break;




    }

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
