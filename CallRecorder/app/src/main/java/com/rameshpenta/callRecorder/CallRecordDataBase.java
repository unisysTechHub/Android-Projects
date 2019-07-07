package com.rameshpenta.callRecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

/**
 * Created by Sujatha on 03-08-2015.
 */
public class CallRecordDataBase  {
    Context context;

    CallRecordDataBaseHelper callRecordDatabaseHelper;
    SQLiteDatabase callRecordDatabase;

    CallRecordDataBase(Context context)
    {
        this.context=context;
        callRecordDatabaseHelper= new CallRecordDataBaseHelper(context,"CallRecordDataBase",null,1);

            }

    public void openDatabase()
    {

                    callRecordDatabase=callRecordDatabaseHelper.getWritableDatabase();
    }

    public void closeDatabase()
    {
        callRecordDatabase.close();

    }

    public boolean isCallingFirstTime(String phoneNumber)
    {
        System.out.println("Ramesh isCallingFirstTIme");
        boolean isFirstTimeCaller=false;
        Cursor cursor=callRecordDatabase.query("CALLER", new String[]{"NUMBER_OF_CALLS"}, "PHONE_NUMBER=?", new String[]{phoneNumber}, null, null, null);

        if(cursor.getCount() == 0) {
            isFirstTimeCaller = true;

        }


        System.out.println(" Ramesh IscallingFirstTIme :" + isFirstTimeCaller);
        return isFirstTimeCaller;

    }

    public void insertCALLER(String phoneNumber,String callerName,String image_uri,long callDateTime)
    {


        System.out.println("Ramesh insert data to caller table");
        ContentValues values = new ContentValues();
        values.put("PHONE_NUMBER",phoneNumber);
        values.put("NUMBER_OF_CALLS",0);
        values.put("CALLER_NAME", callerName);
        values.put("CONTACT_IMAGE_URI", image_uri);
        values.put("CALL_DATE_TIME", callDateTime);

        callRecordDatabase.insert("CALLER", null, values);


    }

    public void insertCallRecord(String phoneNumber,String filePath,String callDate,String callTime,long callDateTime,int callDuration)
    {

        System.out.println("Ramesh insert data to call Record  table");
        ContentValues values = new ContentValues();
        values.put("PHONE_NUMBER",phoneNumber);
        values.put("FILE_PATH", filePath);
        values.put("CALL_DATE", callDate);
        values.put("CALL_MONTH",callDate.substring(0,6));
        values.put("CALL_TIME",callTime);
        values.put("CALL_DURATION",callDuration);

        callRecordDatabase.insert("CALL_RECORDER", null, values);

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
        values.put("CALL_DURATION", callDuration);

        callRecordDatabase.insert("CALL_RECORDER_MONTH", null, values);



    }


    void updateCallerWhenContactInfoChanged(String phoneNumber,String callerName,String image_uri)
    {
        System.out.println("Ramesh update when contact info Change");

        ContentValues contentValues = new ContentValues();
        //if(callerName != null) {
            contentValues.put("CALLER_NAME", callerName);
        //}
        //if(image_uri != null) {
            contentValues.put("CONTACT_IMAGE_URI", image_uri);
        //}
        callRecordDatabase.update("CALLER", contentValues, "PHONE_NUMBER=?", new String[]{phoneNumber});

    }

    void updateCallerTableNoOfCalls(String phoneNumber, boolean fromDeleteFunction,long callDateTime)
    {
        int numberOfCalls;
        ContentValues values = new ContentValues();
        System.out.println("Ramesh updateCallerTableNOofCalls");
        Cursor cursor=callRecordDatabase.query("CALLER", new String[]{"NUMBER_OF_CALLS","CALL_DATE_TIME"}, "PHONE_NUMBER=?", new String[]{phoneNumber}, null, null, null);

        if(cursor.getCount() != 0) {
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
            } else
            {

                numberOfCalls = cursor.getInt(0) + 1;
                if(callDateTime > cursor.getLong(1))
                    values.put("CALL_DATE_TIME", callDateTime);
                //values.put("CALL_DATE_TIME", callDateTime);
        }
        System.out.println("Ramesh no of calls " + numberOfCalls);

            values.put("NUMBER_OF_CALLS", numberOfCalls);

            callRecordDatabase.update("CALLER", values, "PHONE_NUMBER=?", new String[]{phoneNumber});
            if(numberOfCalls == 0)
                deleteFromCallerTableByPhoneNumber(phoneNumber);
        }


    }
    public void deleteFromCallerTableByPhoneNumber(String phoneNumber)
    {
        System.out.println("Ramesh delete From Caller Table By PhoneNUmber");
        callRecordDatabase.delete("CALLER","PHONE_NUMBER=?",new String[]{phoneNumber});



    }


    public Cursor getCallerInfoByPhoneNumber(String phoneNumber)
    {
        System.out.println("Ramesh getCallerInfoByPhone Number");

        Cursor cursor=callRecordDatabase.query("CALLER",new String[]{"_id","PHONE_NUMBER","CALLER_NAME","NUMBER_OF_CALLS","CONTACT_IMAGE_URI","CALL_DATE_TIME"},"PHONE_NUMBER=?",new String[]{phoneNumber},null,null,null);

        return cursor;

    }
    public Cursor getCallerInfo()
    {

            System.out.println("Ramesh getCallerInfo");
        String fDate= Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getLong("setDate", 0)).toString();
        Cursor cursor=callRecordDatabase.query("CALLER C,CALL_RECORDER CR",new String[]{"C._id","C.PHONE_NUMBER","C.CALLER_NAME","C.NUMBER_OF_CALLS","C.CONTACT_IMAGE_URI",
                "SUM(CR.CALL_DURATION)","COUNT(C.PHONE_NUMBER)"},"CR.PHONE_NUMBER=C.PHONE_NUMBER AND CR.CALL_DATE>=?",new String[]{fDate},"C.CALL_DATE_TIME",null,"C.CALL_DATE_TIME DESC");


        return cursor;

    }


    public Cursor getCallerInfoSortByName()
    {
        System.out.println("Ramesh getCallerInfoSortByName");
        String fDate= Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getLong("setDate", 0)).toString();
        //Cursor cursor=callRecordDatabase.query("CALLER",new String[]{"_id","PHONE_NUMBER","CALLER_NAME","NUMBER_OF_CALLS","CONTACT_IMAGE_URI"},null,null,null,null,"CALLER_NAME ASC");
        Cursor cursor=callRecordDatabase.query("CALLER C,CALL_RECORDER CR",new String[]{"C._id","C.PHONE_NUMBER","C.CALLER_NAME","C.NUMBER_OF_CALLS","C.CONTACT_IMAGE_URI","SUM(CR.CALL_DURATION)","COUNT(C.PHONE_NUMBER)"},
                "CR.PHONE_NUMBER=C.PHONE_NUMBER AND CR.CALL_DATE>=?",new String[]{fDate},"C.CALL_DATE_TIME",null,"CALLER_NAME ASC");

        return cursor;

    }

    public Cursor getCallerRecords(String phoneNumber)
    {
            String fDate= Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getLong("setDate", 0)).toString();
        System.out.println("Ramesh getCallerRecords by phonNumber");
        Cursor cursor = callRecordDatabase.query("CALL_RECORDER",new String[]{"_id","PHONE_NUMBER","FILE_PATH","CALL_DATE","CALL_TIME","CALL_DURATION"},"PHONE_NUMBER=? AND CALL_DATE>=? ",
                new String[]{phoneNumber,fDate},null,null,"CALL_DATE DESC,CALL_TIME DESC");

        return cursor;
    }


    public Cursor getCallerRecordsUniqueMonth()
    {
        System.out.println("Ramesh getCallerRecords Unique Month");
        Cursor cursor = callRecordDatabase.query("CALL_RECORDER_MONTH",new String[]{"DISTINCT CALL_MONTH","_id","FILE_PATH","CALL_TIME"},null,null,"CALL_MONTH",null,"CALL_MONTH DESC");

        return cursor;
    }
    public Cursor getCallerRecordsUniqueDate()
    {
        System.out.println("Ramesh getCallerRecordsUniqueDate");
        Cursor cursor = callRecordDatabase.query("CALL_RECORDER",new String[]{"DISTINCT CALL_DATE","_id","FILE_PATH","CALL_TIME","SUM(CALL_DURATION)"},null,null,"CALL_DATE",null,"CALL_DATE DESC");

        return cursor;
    }

    public Cursor getCallerRecordsByDate(String date)
    {
        System.out.println("Ramesh get CallerRecordsBy Date");
        Cursor cursor = callRecordDatabase.query("CALL_RECORDER",new String[]{"_id","FILE_PATH","PHONE_NUMBER","CALL_DATE","CALL_TIME","CALL_DURATION"},"CALL_DATE=?",new String[]{date},null,null,
                "CALL_DATE DESC,CALL_TIME DESC ");

        return cursor;
    }
    public Cursor getCallerRecordsByMonth(String YYYYMMOfDate)
    {
        System.out.println("Ramesh get CallerRecordsBy Date");
        Cursor cursor = callRecordDatabase.query("CALL_RECORDER_MONTH",new String[]{"_id","FILE_PATH","PHONE_NUMBER","CALL_DATE","CALL_TIME","CALLER_NAME","CALL_DURATION"},"CALL_MONTH=?",new String[]{YYYYMMOfDate},null,null,
                "CALL_DATE DESC,CALL_TIME DESC ");

        return cursor;
    }

    public  Cursor callerTableSearchSortByName(String search)
    {
        System.out.println("Ramesh get callerTable Search");
        //Cursor cursor=callRecordDatabase.query("TRASH_CALLER C,TRASH_CALL_RECORDER CR",new String[]{"C._id","C.PHONE_NUMBER","C.CALLER_NAME","C.NUMBER_OF_CALLS","C.CONTACT_IMAGE_URI","SUM(CR.CALL_DURATION)"},"CR.PHONE_NUMBER=C.PHONE_NUMBER",
        //        null,"C.CALL_DATE_TIME",null,"CALLER_NAME ASC");
        String fDate= Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getLong("setDate", 0)).toString();
        Cursor cursor=callRecordDatabase.query("CALLER C,CALL_RECORDER CR",new String[]{"C._id","C.PHONE_NUMBER","C.CALLER_NAME","C.NUMBER_OF_CALLS","C.CONTACT_IMAGE_URI","SUM(CR.CALL_DURATION)","COUNT(C.PHONE_NUMBER)"},
                "CR.PHONE_NUMBER=C.PHONE_NUMBER AND (C.PHONE_NUMBER LIKE? OR C.CALLER_NAME LIKE?) AND CR.CALL_DATE>=?",new String[]{"%"+search+"%","%"+search+"%",fDate},"C.CALL_DATE_TIME",null,"C.CALLER_NAME ASC");

        return cursor;


    }
    public  Cursor callerTableSearch(String search)
    {
        System.out.println("Ramesh get callerTable Search");
        String fDate= Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getLong("setDate", 0)).toString();
        Cursor cursor=callRecordDatabase.query("CALLER C,CALL_RECORDER CR",new String[]{"C._id","C.PHONE_NUMBER","C.CALLER_NAME","C.NUMBER_OF_CALLS","C.CONTACT_IMAGE_URI","SUM(CR.CALL_DURATION)","COUNT(C.PHONE_NUMBER)"},
                "CR.PHONE_NUMBER=C.PHONE_NUMBER AND (C.PHONE_NUMBER LIKE? OR C.CALLER_NAME LIKE?) AND CR.CALL_DATE>=?",new String[]{"%"+search+"%","%"+search+"%",fDate},"C.CALL_DATE_TIME",null,"CALL_DATE_TIME DESC");

        return cursor;


    }
    public Cursor getCallerRecordsUniqueMonthBySearch(String search)
    {
        System.out.println("Ramesh getCallerRecordsUniqueMonth By Search");
        Cursor cursor = callRecordDatabase.query("CALL_RECORDER_MONTH",new String[]{"DISTINCT CALL_DATE","_id","CALL_MONTH","FILE_PATH","CALL_TIME"},"CALL_MONTH LIKE?",new String[]{"%"+search+"%"},"CALL_MONTH",null,"CALL_DATE DESC");

        return cursor;
    }


    public Cursor getCallerRecordsUniqueDateBySearch(String search)
    {
        System.out.println("Ramesh getCallerRecordsUniqueDate By Search");
        Cursor cursor = callRecordDatabase.query("CALL_RECORDER",new String[]{"DISTINCT CALL_DATE","_id","FILE_PATH","CALL_TIME","SUM(CALL_DURATION)"},"CALL_DATE LIKE?",new String[]{"%"+search+"%"},"CALL_DATE",null,"CALL_DATE DESC");

        return cursor;
    }

    public Cursor getCallerRecordByFilePath(String filePath)
    {
        System.out.println("Ramesh get CallerRecordsBy FilePath");
        Cursor cursor = callRecordDatabase.query("CALL_RECORDER", new String[]{"_id", "FILE_PATH", "PHONE_NUMBER", "CALL_DATE", "CALL_TIME","CALL_DURATION"}, "FILE_PATH=?", new String[]{filePath}, null, null,
                null);

        return cursor;
    }
    public Cursor getCallerRecordsByPhoneNumber(String phoneNumber)
    {
        System.out.println("Ramesh get CallerRecordsBy phoneNumber" + phoneNumber);
        Cursor cursor = callRecordDatabase.query("CALL_RECORDER", new String[]{"_id", "FILE_PATH", "PHONE_NUMBER", "CALL_DATE", "CALL_TIME","CALL_DURATION"}, "PHONE_NUMBER=?", new String[]{phoneNumber}, null, null,
                null);

        return cursor;
    }
    public Cursor getCallerRecordByCallDate(String callDate)
    {
        System.out.println("Ramesh get CallerRecordsBy FilePath");
        Cursor cursor = callRecordDatabase.query("CALL_RECORDER", new String[]{"_id", "FILE_PATH", "PHONE_NUMBER", "CALL_DATE", "CALL_TIME", "CALL_DURATION"}, "CALL_DATE=?", new String[]{callDate}, null, null,
                null);

        return cursor;
    }

    public Cursor getAllCallRecords()
    {
        System.out.println("Ramesh getAll Call Records");

        Cursor cursor = callRecordDatabase.query("CALL_RECORDER", new String[]{"_id", "FILE_PATH", "PHONE_NUMBER", "CALL_DATE", "CALL_TIME", "CALL_DURATION"}, null, null, null, null,
                null);

        return cursor;
    }

    public Cursor getAllCallRecordsMonth()
    {
        System.out.println("Ramesh getAll Call Records");

        Cursor cursor = callRecordDatabase.query("CALL_RECORDER_MONTH", new String[]{"_id", "FILE_PATH", "PHONE_NUMBER", "CALL_DATE", "CALL_TIME", "CALL_DURATION"}, null, null, null, null,
                null);

        return cursor;
    }


    public void emptyInboxDatabase(boolean sortByMonth)
    {
        String filePath;
        System.out.println("Ramesh empty Inbox Databse");
        Cursor cursor = getAllCallRecords();
        while(cursor.moveToNext())
        {

            filePath = cursor.getString(cursor.getColumnIndex("FILE_PATH"));
            moveRecordsToTrashDatabase(filePath,sortByMonth);

        }

        cursor.close();


    }


    public void deleteFromCallerRecordTableByFilePath(String filePath)
    {

        System.out.println("Ramesh delete from Caller Record Table By filePath");
        callRecordDatabase.delete("CALL_RECORDER", "FILE_PATH=?", new String[]{filePath});


    }

    public  void moveRecordsToTrashDatabaseByCallDate(String callDateForDelete,boolean sortByDate)
    {
        Cursor cursor;
        String phoneNumber;
        String filePath;
        String callTime;
        int callDuration;
        String callerName;
        String image_uri;
        long callDateTime;
        String callDate;
        System.out.println("Ramesh Move Records to TrashDatabase");
        if(sortByDate) {
             cursor = getCallerRecordsByDate(callDateForDelete);
        }
        else {
             cursor = getCallerRecordsByMonth(callDateForDelete);
        }
        if(sortByDate)
        {

            CallRecordTrashDatabase callRecordTrashDatabase = new CallRecordTrashDatabase(context);
            callRecordTrashDatabase.openDatabase();
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
                if (callRecordTrashDatabase.isCallingFirstTime(phoneNumber)) {

                    Long callDateTimeTrash = Long.parseLong(callDateForDelete + callTime.replace(":", "") + "00");

                    callRecordTrashDatabase.insertCALLER(phoneNumber, callerName, image_uri, callDateTimeTrash);


                }
                if (sortByDate)
                    callRecordTrashDatabase.insertCallRecord(phoneNumber, filePath, callDate, callTime, callDuration, callDateTime);
                else {


                }
                System.out.println("Ramesh phone NUmber " + phoneNumber);


                deleteFromCallerRecordTableByFilePath(filePath);
                updateCallerTableNoOfCalls(phoneNumber, true, 0);
                cursorCallerTable.close();
            }

        }

    }

    public  void moveRecordsToTrashDatabaseByPhoneNumber(String phoneNumber)
    {
        System.out.println("Ramesh Move Records to TrashDatabase");
        Cursor cursor = getCallerRecordsByPhoneNumber(phoneNumber);
        CallRecordTrashDatabase callRecordTrashDatabase = new CallRecordTrashDatabase(context);
        callRecordTrashDatabase.openDatabase();
        while(cursor.moveToNext()) {
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
            if (callRecordTrashDatabase.isCallingFirstTime(phoneNumber)) {

                Long callDateTimeTrash = Long.parseLong(callDate + callTime.replace(":", "") + "00");

                callRecordTrashDatabase.insertCALLER(phoneNumber, callerName, image_uri, callDateTimeTrash);


            }

            callRecordTrashDatabase.insertCallRecord(phoneNumber, filePath, callDate, callTime, callDuration, callDateTime);
            System.out.println("Ramesh phone NUmber " + phoneNumber);


            deleteFromCallerRecordTableByFilePath(filePath);
            updateCallerTableNoOfCalls(phoneNumber, true, 0);
            cursorCallerTable.close();
        }

    }

    public  void moveRecordsToTrashDatabase(String filePath,boolean sortByMonth)
    {
        if(!sortByMonth)
        {
            System.out.println("Ramesh Move Records to TrashDatabase");
            Cursor cursor = getCallerRecordByFilePath(filePath);
            cursor.moveToFirst();
            String phoneNumber = cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"));
            String callDate = cursor.getString(cursor.getColumnIndex("CALL_DATE"));
            String callTime = cursor.getString(cursor.getColumnIndex("CALL_TIME"));
            int callDuration = cursor.getInt(cursor.getColumnIndex("CALL_DURATION"));

            CallRecordTrashDatabase callRecordTrashDatabase = new CallRecordTrashDatabase(context);
            callRecordTrashDatabase.openDatabase();
            Cursor cursorCallerTable = getCallerInfoByPhoneNumber(phoneNumber);
            if (cursorCallerTable == null)
                System.out.println("Ramesh cursorcaller table null");
            cursorCallerTable.moveToFirst();
            String callerName = cursorCallerTable.getString(cursorCallerTable.getColumnIndex("CALLER_NAME"));
            String image_uri = cursorCallerTable.getString(cursorCallerTable.getColumnIndex("CONTACT_IMAGE_URI"));
            long callDateTime = cursorCallerTable.getLong(cursorCallerTable.getColumnIndex("CALL_DATE_TIME"));
            if (callRecordTrashDatabase.isCallingFirstTime(phoneNumber)) {

                Long callDateTimeTrash = Long.parseLong(callDate + callTime.replace(":", "") + "00");

                callRecordTrashDatabase.insertCALLER(phoneNumber, callerName, image_uri, callDateTimeTrash);


            }

            callRecordTrashDatabase.insertCallRecord(phoneNumber, filePath, callDate, callTime, callDuration, callDateTime);
            System.out.println("Ramesh phone NUmber " + phoneNumber);


            deleteFromCallerRecordTableByFilePath(filePath);
            updateCallerTableNoOfCalls(phoneNumber, true, 0);
            cursorCallerTable.close();

        }


    }




    class CallRecordDataBaseHelper extends SQLiteOpenHelper
    {



        public CallRecordDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            System.out.println("Ramesh Databse Oncreate");

            db.execSQL("CREATE TABLE CALLER " +
                    "(" +
                    " _id INTEGER PRIMARY KEY," +
                    "PHONE_NUMBER TEXT, " +
                    "CALLER_NAME TEXT," +
                    "NUMBER_OF_CALLS INTEGER, " +
                    "CONTACT_IMAGE_URI TEXT," +
                    "CALL_DATE_TIME INTEGER" +
                    ")");

            db.execSQL("CREATE TABLE CALL_RECORDER " +
                    "( " +
                    " _id INTEGER PRIMARY KEY," +
                    "PHONE_NUMBER TEXT, " +
                    "FILE_PATH TEXT," +
                    "CALL_DATE TEXT," +
                    "CALL_MONTH TEXT," +
                    "CALL_TIME TEXT," +
                    "CALL_DURATION INTEGER" +

                    ")");

            db.execSQL("CREATE TABLE CALL_RECORDER_MONTH " +
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
            System.out.println("Ramesh onUpgrade");

        }
    }
}
