package com.rameshpenta.callRecorder;

import android.net.Uri;

/**
 * Created by Sujatha on 12-08-2015.
 */
public class Utils {


    final  public static int CALLER_TABLE_ByNAME = 101;
    final public static int CALL_RECORDER_TABLE_ByPhoneNumber = 102;
    final public static int CALL_RECORDER_TABLE_ByDATE = 103;
    final public static int CALL_RECORDER_TABLE_UNIQUE_DATE = 104;
    final public static int CALLER_TABLE_SEARCH = 105;
    final public static Uri CALLER_TABLE_URI = Uri.parse("content://callRecorderApp.database/CALLERTable");
    final public static Uri CALL_RECORDER_TABLE_BY_PHONE_NUMBER = Uri.parse("content://callRecorderApp.database/CALLRECORDERTableByPhoneNumber");
    final public static Uri CALL_RECORDER_TABLE_BY_DATE_URI = Uri.parse("content://callRecorderApp.database/CALLRECORDERTableByDate");
    final public static Uri CALL_RECORDER_TABLE_UNIQUE_DATE_URI = Uri.parse("content://callRecorderApp.database/CALLRECORDERTableUniqueDate");
    final public static Uri CALLER_TABLE_SEARCH_URI = Uri.parse("content://callRecorderApp.database/CALLERTableSearch");

    public static  String getMonthName(int month)
    {
        String monthName=null;
            switch (month)
            {
                case 1:
                    monthName="Jan";
                    break;
                case 2:
                    monthName="Feb";
                    break;
                case 3:
                    monthName="Mar";
                    break;
                case 4:
                    monthName="Apr";
                    break;
                case 5:
                    monthName="May";
                    break;
                case 6:
                    monthName="Jun";
                    break;
                case 7:
                    monthName="Jul";
                    break;
                case 8:
                    monthName="Aug";
                    break;
                case 9:
                    monthName="Sep";
                    break;
                case 10:
                    monthName="Oct";
                    break;
                case 11:
                    monthName="Nov";
                    break;
                case 12:
                    monthName="Dec";

            }


        return monthName;
    }




}
