package com.rameshpenta.callRecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;


/**
 * Created by Sujatha on 03-08-2015.
 */
public class CallRecordTrashDatabase  {

    CallRecordTrashDatabaseHelper callRecordTrashDatabaseHelper;
    SQLiteDatabase callRecordTrashDatabase;
    Context context;

    CallRecordTrashDatabase(Context context)
    {
        callRecordTrashDatabaseHelper= new CallRecordTrashDatabaseHelper(context,"CallRecordTrashDatabase",null,1);
        this.context=context;

    }

    public void openDatabase()
    {
        callRecordTrashDatabase=callRecordTrashDatabaseHelper.getWritableDatabase();

    }
    public  void closeDatabase()
    {
        callRecordTrashDatabase.close();


    }

    public boolean isCallingFirstTime(String phoneNumber)
    {
        System.out.println("Ramesh Trash isCallingFirstTIme");
        boolean isFirstTimeCaller=false;
        Cursor cursor=callRecordTrashDatabase.query("TRASH_CALLER", new String[]{"NUMBER_OF_CALLS"}, "PHONE_NUMBER=?", new String[]{phoneNumber}, null, null, null);

        if(cursor.getCount() == 0) {
            isFirstTimeCaller = true;

        }


        System.out.println(" Ramesh Trash IscallingFirstTIme :" + isFirstTimeCaller);
        return isFirstTimeCaller;

    }

    public void insertCALLER(String phoneNumber,String callerName,String image_uri,long callDateTime)
    {


        System.out.println("Ramesh Trash insert data to caller table");
        ContentValues values = new ContentValues();
        values.put("PHONE_NUMBER",phoneNumber);
        values.put("NUMBER_OF_CALLS",0);
        values.put("CALLER_NAME", callerName);
        values.put("CONTACT_IMAGE_URI",image_uri);
        values.put("CALL_DATE_TIME", callDateTime);
        System.out.println("Ramesh1 calldate time :" + callDateTime);
        callRecordTrashDatabase.insert("TRASH_CALLER", null, values);


    }
    public void insertCallRecord(String phoneNumber,String filePath,String callDate,String callTime,int callDuration,long callDateTime)
    {

        System.out.println("Ramesh Trash insert data to call Record  table");
        ContentValues values = new ContentValues();
        values.put("PHONE_NUMBER",phoneNumber);
        values.put("FILE_PATH", filePath);
        values.put("CALL_DATE", callDate);
        values.put("CALL_MONTH", callDate.substring(0,6));
        values.put("CALL_TIME", callTime);
        values.put("CALL_DURATION", callDuration);

        callRecordTrashDatabase.insert("TRASH_CALL_RECORDER", null, values);

        //Update NumberofCalls of Colum in caller Table
        callDateTime = Long.parseLong(callDate+callTime.replace(":","")+"00");
        updateCallerTableNoOfCalls(phoneNumber, false, callDateTime);


    }
    public void insertCallRecordForMonthlyReport(String phoneNumber,String filePath,String callDate,String callTime,long callDateTime,int callDuration,String callerName)
    {

        System.out.println("Ramesh insert data to call Record  table");
        ContentValues values = new ContentValues();
        values.put("PHONE_NUMBER",phoneNumber);
        values.put("FILE_PATH", filePath);
        values.put("CALL_DATE", callDate);
        values.put("CALLER_NAME", callerName);
        values.put("CALL_MONTH",callDate.substring(0,6));
        values.put("CALL_TIME", callTime);
        values.put("CALL_DURATION",callDuration);

        callRecordTrashDatabase.insert("TRASH_CALL_RECORDER_MONTH", null, values);



    }


    void updateCallerWhenContactInfoChanged(String phoneNumber,String callerName,String image_uri)
    {
        System.out.println("Ramesh update when contact info Change");

        ContentValues contentValues = new ContentValues();
       // if(callerName != null) {
            contentValues.put("CALLER_NAME", callerName);
        //}
       // if(image_uri != null) {
            contentValues.put("CONTACT_IMAGE_URI", image_uri);
        //}
        callRecordTrashDatabase.update("TRASH_CALLER", contentValues, "PHONE_NUMBER=?", new String[]{phoneNumber});

    }


    void updateCallerTableNoOfCalls(String phoneNumber, boolean fromDeleteFunction,long callDateTime)
    {

        int numberOfCalls;
        System.out.println("Ramesh updateCallerTableNOofCalls");
        Cursor cursor = callRecordTrashDatabase.query("TRASH_CALLER", new String[]{"NUMBER_OF_CALLS", "CALL_DATE_TIME"}, "PHONE_NUMBER=?", new String[]{phoneNumber}, null, null, null);


        ContentValues values = new ContentValues();
        if (cursor.getCount() != 0) {
            cursor.moveToNext();
            if (fromDeleteFunction) {
                numberOfCalls = cursor.getInt(0) - 1;

                Cursor cursorCallRecorder = getCallerRecords(phoneNumber);
                if(cursorCallRecorder.getCount() > 0) {

                    cursorCallRecorder.moveToNext();
                    String latestCallDateTime = cursorCallRecorder.getString(cursorCallRecorder.getColumnIndex("CALL_DATE")) +
                            cursorCallRecorder.getString(cursorCallRecorder.getColumnIndex("CALL_TIME")).replace(":", "");


                    values.put("CALL_DATE_TIME", Long.parseLong(latestCallDateTime + "00"));
                }
            } else {

                numberOfCalls = cursor.getInt(0) + 1;
               // System.out.println("Ramesh1 callDatetime :" + callDateTime + cursor.getLong(cursor.getColumnIndex("CALL_DATE_TIME")));
                if(callDateTime > cursor.getLong(1))
                    values.put("CALL_DATE_TIME", callDateTime);

            }
            System.out.println("Ramesh no of calls " + numberOfCalls);

            values.put("NUMBER_OF_CALLS", numberOfCalls);

            callRecordTrashDatabase.update("TRASH_CALLER", values, "PHONE_NUMBER=?", new String[]{phoneNumber});
            if (numberOfCalls == 0)
                deleteFromCallerTableByPhoneNumber(phoneNumber);
        }

    }

    public void deleteFromCallerTableByPhoneNumber(String phoneNumber)
    {
        System.out.println("Ramesh delete From Caller Table By PhoneNUmber");
        callRecordTrashDatabase.delete("TRASH_CALLER", "PHONE_NUMBER=?", new String[]{phoneNumber});


    }



    public Cursor getCallerInfo()
    {
            System.out.println("Ramesh Trash getCallerInfo");
        Cursor cursor=callRecordTrashDatabase.query("TRASH_CALLER C,TRASH_CALL_RECORDER CR",new String[]{"C._id","C.PHONE_NUMBER","C.CALLER_NAME","C.NUMBER_OF_CALLS","C.CONTACT_IMAGE_URI","SUM(CR.CALL_DURATION)"},"CR.PHONE_NUMBER=C.PHONE_NUMBER",null,"C.CALL_DATE_TIME",null,"C.CALL_DATE_TIME DESC");

//        Cursor cursor=callRecordTrashDatabase.query("TRASH_CALLER",new String[]{"_id","PHONE_NUMBER","CALLER_NAME","NUMBER_OF_CALLS","CONTACT_IMAGE_URI"},null,null,null,null,"CALL_DATE_TIME DESC");

        return cursor;

    }

    public Cursor getCallerInfoSortByName()
    {
        System.out.println("Ramesh Trash getCallerInfoSortByName");

        //Cursor cursor=callRecordTrashDatabase.query("TRASH_CALLER",new String[]{"_id","PHONE_NUMBER","CALLER_NAME","NUMBER_OF_CALLS","CONTACT_IMAGE_URI"},null,null,null,null,"CALLER_NAME ASC");
        Cursor cursor=callRecordTrashDatabase.query("TRASH_CALLER C,TRASH_CALL_RECORDER CR",new String[]{"C._id","C.PHONE_NUMBER","C.CALLER_NAME","C.NUMBER_OF_CALLS","C.CONTACT_IMAGE_URI","SUM(CR.CALL_DURATION)"},"CR.PHONE_NUMBER=C.PHONE_NUMBER",null,"C.CALL_DATE_TIME",null,"CALLER_NAME ASC");

        return cursor;

    }

    public Cursor getCallerRecords(String phoneNumber)
    {
        System.out.println("Ramesh Trash getCallerRecords");
        Cursor cursor = callRecordTrashDatabase.query("TRASH_CALL_RECORDER",new String[]{"_id","PHONE_NUMBER","FILE_PATH","CALL_DATE","CALL_TIME","CALL_DURATION"},"PHONE_NUMBER=?",new String[]{phoneNumber},null,null,"CALL_DATE DESC");

        return cursor;
    }
    public Cursor getCallerRecordsUniqueMonth()
    {
        System.out.println("Ramesh getCallerRecordsUniqueMonth");
        Cursor cursor = callRecordTrashDatabase.query("TRASH_CALL_RECORDER_MONTH",new String[]{"DISTINCT CALL_MONTH","_id","FILE_PATH","CALL_TIME"},null,null,"CALL_MONTH",null,"CALL_MONTH DESC");

        return cursor;
    }
    public Cursor getCallerRecordsByMonth(String YYYYMMOfDate)
    {
        System.out.println("Ramesh get CallerRecordsBy Date");
        Cursor cursor = callRecordTrashDatabase.query("TRASH_CALL_RECORDER_MONTH",new String[]{"_id","FILE_PATH","PHONE_NUMBER","CALL_DATE","CALL_TIME","CALLER_NAME","CALL_DURATION"},"CALL_MONTH=?",new String[]{YYYYMMOfDate},null,null,
                "CALL_DATE DESC,CALL_TIME DESC ");

        return cursor;
    }

    public Cursor getCallerRecordsUniqueDate()
    {
        System.out.println("Ramesh Trash getCallerRecordsUniqueDate");
        Cursor cursor = callRecordTrashDatabase.query("TRASH_CALL_RECORDER",new String[]{"DISTINCT CALL_DATE","_id","FILE_PATH","CALL_TIME","SUM(CALL_DURATION)"},null,null,"CALL_DATE",null,"CALL_DATE DESC");

        return cursor;
    }

    public Cursor getCallerRecordsByDate(String date)
    {
        System.out.println("Ramesh Trash getCallerRecordsByDate");
        Cursor cursor = callRecordTrashDatabase.query("TRASH_CALL_RECORDER",new String[]{"_id","FILE_PATH","PHONE_NUMBER","CALL_DATE","CALL_TIME","CALL_DURATION"},"CALL_DATE=?",new String[]{date},null,null,"CALL_DATE DESC");

        return cursor;
    }



    public  Cursor callerTableSearchSortByName(String search)
    {
        System.out.println("Ramesh get callerTable Search");
        //Cursor cursor=callRecordTrashDatabase.query("TRASH_CALLER",new String[]{"_id","PHONE_NUMBER","CALLER_NAME","NUMBER_OF_CALLS","CONTACT_IMAGE_URI"},
          //      "PHONE_NUMBER LIKE? OR CALLER_NAME LIKE?",new String[]{"%"+search+"%","%"+search+"%"},null,null,"CALLER_NAME ASC");
        Cursor cursor=callRecordTrashDatabase.query("TRASH_CALLER C,TRASH_CALL_RECORDER CR",new String[]{"C._id","C.PHONE_NUMBER","C.CALLER_NAME","C.NUMBER_OF_CALLS","C.CONTACT_IMAGE_URI","SUM(CR.CALL_DURATION)"},
                "CR.PHONE_NUMBER=C.PHONE_NUMBER AND (C.PHONE_NUMBER LIKE? OR C.CALLER_NAME LIKE?)",new String[]{"%"+search+"%","%"+search+"%"},"C.CALL_DATE_TIME",null,"C.CALLER_NAME ASC");


        return cursor;


    }
    public  Cursor callerTableSearch(String search)
    {
        System.out.println("Ramesh get callerTable Search");
     //   Cursor cursor=callRecordTrashDatabase.query("TRASH_CALLER",new String[]{"_id","PHONE_NUMBER","CALLER_NAME","NUMBER_OF_CALLS","CONTACT_IMAGE_URI"},
       //         "PHONE_NUMBER LIKE? OR CALLER_NAME LIKE?",new String[]{"%"+search+"%","%"+search+"%"},null,null,"CALL_DATE_TIME DESC");
        Cursor cursor=callRecordTrashDatabase.query("TRASH_CALLER C,TRASH_CALL_RECORDER CR",new String[]{"C._id","C.PHONE_NUMBER","C.CALLER_NAME","C.NUMBER_OF_CALLS","C.CONTACT_IMAGE_URI","SUM(CR.CALL_DURATION)"},
                "CR.PHONE_NUMBER=C.PHONE_NUMBER AND (C.PHONE_NUMBER LIKE? OR C.CALLER_NAME LIKE?)",new String[]{"%"+search+"%","%"+search+"%"},"C.CALL_DATE_TIME",null,"CALL_DATE_TIME DESC");

        return cursor;


    }
    public Cursor getCallerRecordsUniqueMonthBySearch(String search)
    {
        System.out.println("Ramesh getCallerRecordsUniqueDate Month Search");
        Cursor cursor = callRecordTrashDatabase.query("TRASH_CALL_RECORDER_MONTH",new String[]{"DISTINCT CALL_DATE","_id","FILE_PATH","CALL_TIME"},"CALL_MONTH LIKE?",new String[]{"%"+search+"%"},"CALL_DATE",null,"CALL_DATE DESC");

        return cursor;
    }


    public Cursor getCallRecordsByPhoneNumber(String phoneNumber)
    {
        System.out.println("Ramesh get Call Records By phoneNumber");
        Cursor cursor = callRecordTrashDatabase.query("TRASH_CALL_RECORDER", new String[]{"_id", "FILE_PATH", "PHONE_NUMBER", "CALL_DATE", "CALL_TIME","CALL_DURATION"}, "PHONE_NUMBER=?", new String[]{phoneNumber}, null, null,
                null);

        return cursor;
    }
    public Cursor getCallerRecordByFilePath(String filePath)
    {
        System.out.println("Ramesh get CallerRecordsBy FilePath");
        Cursor cursor = callRecordTrashDatabase.query("TRASH_CALL_RECORDER", new String[]{"_id", "FILE_PATH", "PHONE_NUMBER", "CALL_DATE", "CALL_TIME","CALL_DURATION"}, "FILE_PATH=?", new String[]{filePath}, null, null,
                null);

        return cursor;
    }
    public Cursor getCallerRecordsByCallDate(String callDate)
    {
        System.out.println("Ramesh get CallerRecordsBy FilePath");
        Cursor cursor = callRecordTrashDatabase.query("TRASH_CALL_RECORDER", new String[]{"_id", "FILE_PATH", "PHONE_NUMBER", "CALL_DATE", "CALL_TIME","CALL_DURATION"}, "CALL_DATE=?", new String[]{callDate}, null, null,
                null);

        return cursor;
    }
    public void deleteFromCallerRecordTableByFilePath(String filePath)
    {

        System.out.println("Ramesh delete from Caller Record Table By filePath");
        callRecordTrashDatabase.delete("TRASH_CALL_RECORDER", "FILE_PATH=?", new String[]{filePath});


    }
    public Cursor getCallerRecordsUniqueDateBySearch(String search)
    {
        System.out.println("Ramesh getCallerRecordsUniqueDate By Search");
        Cursor cursor = callRecordTrashDatabase.query("TRASH_CALL_RECORDER",new String[]{"DISTINCT CALL_DATE","_id","FILE_PATH","CALL_TIME","SUM(CALL_DURATION)"},"CALL_DATE LIKE?",new String[]{"%"+search+"%"},"CALL_DATE",null,"CALL_DATE DESC");

        return cursor;
    }
    public Cursor getAllCallRecords()
    {
        System.out.println("Ramesh getAll Call Records");

        Cursor cursor = callRecordTrashDatabase.query("TRASH_CALL_RECORDER", new String[]{"_id", "FILE_PATH", "PHONE_NUMBER", "CALL_DATE", "CALL_TIME","CALL_DURATION"}, null, null, null, null,
                null);

        return cursor;
    }
    public Cursor getCallerInfoByPhoneNumber(String phoneNumber)
    {
        System.out.println("Ramesh getCallerInfoByPhone Number");

        Cursor cursor=callRecordTrashDatabase.query("TRASH_CALLER", new String[]{"_id", "PHONE_NUMBER", "CALLER_NAME", "NUMBER_OF_CALLS", "CONTACT_IMAGE_URI", "CALL_DATE_TIME"}, "PHONE_NUMBER=?", new String[]{phoneNumber}, null, null, null);

        return cursor;

    }

    public void emptyTrashDatabase()
    {
        String filePath;
        System.out.println("Ramesh empty Inbox Databse");
        Cursor cursor = getAllCallRecords();
        while(cursor.moveToNext())
        {

            filePath = cursor.getString(cursor.getColumnIndex("FILE_PATH"));
            String phoneNumber=cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"));
            deleteFromCallerRecordTableByFilePath(filePath);
            File audioFile = new File(filePath);
            audioFile.delete();
            updateCallerTableNoOfCalls(phoneNumber, true,0);

        }


    }
    public  void deleteAudioFilesPermanentlyByPhoneNumber(String phoneNumber)
    {
        System.out.println("Ramesh Move Records to TrashDatabase");
        Cursor cursor = getCallRecordsByPhoneNumber(phoneNumber);
        while(cursor.moveToNext()) {

            String filePath = cursor.getString(cursor.getColumnIndex("FILE_PATH"));
            System.out.println("Ramesh phone NUmber " + phoneNumber);


            deleteFromCallerRecordTableByFilePath(filePath);
         //   File audioFile = new File(filePath);
           // audioFile.delete();
            updateCallerTableNoOfCalls(phoneNumber, true, 0);
        }

    }


    public  void deleteAudioFilesPermanentlyByCallDate(String callDate,boolean sortByDate)
    {
        System.out.println("Ramesh Move Records to TrashDatabase");
        Cursor cursor;
        if(sortByDate)
        cursor = getCallerRecordsByCallDate(callDate);
        else
        cursor=getCallerRecordsByMonth(callDate);
        while(cursor.moveToNext()) {
            String phoneNumber = cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"));
            String filePath = cursor.getString(cursor.getColumnIndex("FILE_PATH"));
            System.out.println("Ramesh phone NUmber " + phoneNumber);

            if(sortByDate)
            deleteFromCallerRecordTableByFilePath(filePath);

            if(sortByDate)
                ;
            //else {
              //  File audioFile = new File(filePath);
               // audioFile.delete();
            //}
            if(sortByDate)
            updateCallerTableNoOfCalls(phoneNumber, true, 0);
        }

    }

    public  void deleteAudioFilesPermanently(String filePath,boolean sortByMonth)
    {
        System.out.println("Ramesh Move Records to TrashDatabase");
        Cursor cursor = getCallerRecordByFilePath(filePath);
        cursor.moveToFirst();
        String phoneNumber=cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"));

        System.out.println("Ramesh phone NUmber " + phoneNumber);

         if(!sortByMonth)
        deleteFromCallerRecordTableByFilePath(filePath);
        //if(sortByMonth) {
          //    File audioFile = new File(filePath);
            // audioFile.delete();
        //}
        if(!sortByMonth)
        updateCallerTableNoOfCalls(phoneNumber, true,0);

    }



    public  void moveRecordsToInbox(String filePath)
    {
        System.out.println("Ramesh Move Records to Inbox");
        Cursor cursor = getCallerRecordByFilePath(filePath);
        cursor.moveToFirst();
        String phoneNumber=cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"));
        String callDate=cursor.getString(cursor.getColumnIndex("CALL_DATE"));
        String callTime =cursor.getString(cursor.getColumnIndex("CALL_TIME"));
        int callDuration=cursor.getInt(cursor.getColumnIndex("CALL_DURATION"));

        CallRecordDataBase callRecordDatabase = new CallRecordDataBase(context);
        callRecordDatabase.openDatabase();
        Cursor cursorCallerTable=getCallerInfoByPhoneNumber(phoneNumber);
        if(cursorCallerTable== null)
            System.out.println("Ramesh cursorcaller table null");
        cursorCallerTable.moveToFirst();
        String callerName = cursorCallerTable.getString(cursorCallerTable.getColumnIndex("CALLER_NAME"));
        String image_uri = cursorCallerTable.getString(cursorCallerTable.getColumnIndex("CONTACT_IMAGE_URI"));
        long callDateTime = cursorCallerTable.getLong(cursorCallerTable.getColumnIndex("CALL_DATE_TIME"));
        if(callRecordDatabase.isCallingFirstTime(phoneNumber))
        {

            Long latestCallDateTime = Long.parseLong(callDate+callTime.replace(":","")+"00");
            callRecordDatabase.insertCALLER(phoneNumber,callerName,image_uri,latestCallDateTime);


        }

        callRecordDatabase.insertCallRecord(phoneNumber, filePath, callDate, callTime,callDateTime,callDuration);
        System.out.println("Ramesh phone NUmber " + phoneNumber);


        deleteFromCallerRecordTableByFilePath(filePath);
        updateCallerTableNoOfCalls(phoneNumber, true,0);
        cursorCallerTable.close();

    }

    public  void moveRecordsToInboxByPhoneNumber(String phoneNumber)
    {
        System.out.println("Ramesh Move Records to Inbox");
        Cursor cursor = getCallRecordsByPhoneNumber(phoneNumber);
        CallRecordDataBase callRecordDatabase = new CallRecordDataBase(context);
        callRecordDatabase.openDatabase();
        while (cursor.moveToNext()) {
            String callDate = cursor.getString(cursor.getColumnIndex("CALL_DATE"));
            String filePath = cursor.getString(cursor.getColumnIndex("FILE_PATH"));
            String callTime = cursor.getString(cursor.getColumnIndex("CALL_TIME"));
            int callDuration = cursor.getInt(cursor.getColumnIndex("CALL_DURATION"));


            Cursor cursorCallerTable = getCallerInfoByPhoneNumber(phoneNumber);
            if (cursorCallerTable == null)
                System.out.println("Ramesh cursorcaller table null");
            cursorCallerTable.moveToFirst();
            String callerName = cursorCallerTable.getString(cursorCallerTable.getColumnIndex("CALLER_NAME"));
            String image_uri = cursorCallerTable.getString(cursorCallerTable.getColumnIndex("CONTACT_IMAGE_URI"));
            long callDateTime = cursorCallerTable.getLong(cursorCallerTable.getColumnIndex("CALL_DATE_TIME"));
            if (callRecordDatabase.isCallingFirstTime(phoneNumber)) {

                Long latestCallDateTime = Long.parseLong(callDate + callTime.replace(":", "") + "00");
                callRecordDatabase.insertCALLER(phoneNumber, callerName, image_uri, latestCallDateTime);


            }

            callRecordDatabase.insertCallRecord(phoneNumber, filePath, callDate, callTime, callDateTime, callDuration);
            System.out.println("Ramesh phone NUmber " + phoneNumber);


            deleteFromCallerRecordTableByFilePath(filePath);
            updateCallerTableNoOfCalls(phoneNumber, true, 0);
            cursorCallerTable.close();
        }

    }



    public  void moveRecordsToInboxByCallDate(String callDate,boolean sortByDate)
    {
        System.out.println("Ramesh Move Records to Inbox");
        Cursor cursor;
        String phoneNumber;
        String filePath;
        String callTime;
        int callDuration;
        String callerName;
        String image_uri;
        long callDateTime;
        Long latestCallDateTime;
        if(sortByDate)
         cursor = getCallerRecordsByCallDate(callDate);
        else
        cursor=getCallerRecordsByMonth(callDate);

        CallRecordDataBase callRecordDatabase = new CallRecordDataBase(context);
        callRecordDatabase.openDatabase();
        while (cursor.moveToNext()) {
             phoneNumber = cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"));
             filePath = cursor.getString(cursor.getColumnIndex("FILE_PATH"));
             callTime = cursor.getString(cursor.getColumnIndex("CALL_TIME"));
             callDate = cursor.getString(cursor.getColumnIndex("CALL_DATE"));
            callDuration = cursor.getInt(cursor.getColumnIndex("CALL_DURATION"));



            Cursor cursorCallerTable = getCallerInfoByPhoneNumber(phoneNumber);
            if (cursorCallerTable == null)
                System.out.println("Ramesh cursorcaller table null");
            cursorCallerTable.moveToFirst();
             callerName = cursorCallerTable.getString(cursorCallerTable.getColumnIndex("CALLER_NAME"));
             image_uri = cursorCallerTable.getString(cursorCallerTable.getColumnIndex("CONTACT_IMAGE_URI"));
            callDateTime = cursorCallerTable.getLong(cursorCallerTable.getColumnIndex("CALL_DATE_TIME"));
            if (callRecordDatabase.isCallingFirstTime(phoneNumber)) {

                latestCallDateTime = Long.parseLong(callDate + callTime.replace(":", "") + "00");
                callRecordDatabase.insertCALLER(phoneNumber, callerName, image_uri, latestCallDateTime);


            }

            callRecordDatabase.insertCallRecord(phoneNumber, filePath, callDate, callTime, callDateTime, callDuration);
            System.out.println("Ramesh phone NUmber " + phoneNumber);


            deleteFromCallerRecordTableByFilePath(filePath);
            updateCallerTableNoOfCalls(phoneNumber, true, 0);
            cursorCallerTable.close();
        }

    }
















    class CallRecordTrashDatabaseHelper extends SQLiteOpenHelper
    {



        public CallRecordTrashDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            System.out.println("Ramesh Trash Databse Oncreate");

            db.execSQL("CREATE TABLE TRASH_CALLER " +
                    "(" +
                    " _id INTEGER PRIMARY KEY," +
                    "PHONE_NUMBER TEXT, " +
                    "CALLER_NAME TEXT," +
                    "NUMBER_OF_CALLS INTEGER, " +
                    "CONTACT_IMAGE_URI TEXT," +
                    "CALL_DATE_TIME INTEGER" +

                    ")");

            db.execSQL("CREATE TABLE TRASH_CALL_RECORDER " +
                    "( " +
                    " _id INTEGER PRIMARY KEY," +
                    "PHONE_NUMBER TEXT, " +
                    "FILE_PATH TEXT," +
                    "CALL_DATE TEXT," +
                    "CALL_MONTH TEXT," +
                    "CALL_TIME TEXT," +
                    "CALL_DURATION INTEGER" +
                    ")");
            db.execSQL("CREATE TABLE TRASH_CALL_RECORDER_MONTH " +
                    "( " +
                    " _id INTEGER PRIMARY KEY," +
                    "PHONE_NUMBER TEXT, " +
                    "FILE_PATH TEXT," +
                    "CALLER_NAME TEXT," +
                    "CALL_DATE TEXT," +
                    "CALL_MONTH TEXT," +
                    "CALL_TIME TEXT," +
                    "CALL_DURATION INTEGER" +

                    ")");




        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            System.out.println("Ramesh Trash onUpgrade");

        }
    }
}

