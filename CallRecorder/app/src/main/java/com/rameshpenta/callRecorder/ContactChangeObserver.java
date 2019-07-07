package com.rameshpenta.callRecorder;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;

/**
 * Created by Sujatha on 22-10-2015.
 */
public class ContactChangeObserver extends ContentObserver {
    Context appContext;

    String image_uri = null;
    public ContactChangeObserver(Handler handler, Context context) {
        super(handler);


        appContext=context;

        //this.dataBase=dataBase;

            }


    @Override
    public void onChange(boolean selfChange, Uri uri) {
        System.out.println("Ramesh onChange Content Observer :" + uri);
        CallRecordDataBase dataBase = new CallRecordDataBase(appContext);
        dataBase.openDatabase();
       Cursor cursor= dataBase.getCallerInfo();
        String image_uri_caller_table;
        String callerName_caller_table;
        String callerName;
        String phoneNumber;


        while(cursor.moveToNext())
        {


            image_uri_caller_table=cursor.getString(cursor.getColumnIndex("CONTACT_IMAGE_URI"));
            callerName_caller_table=cursor.getString(cursor.getColumnIndex("CALLER_NAME"));
            phoneNumber =cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"));
            callerName = getNameFromContacts(phoneNumber);


            //dataBase.updateCallerWhenContactInfoChanged(phoneNumber, callerName, image_uri);


            if(image_uri != null )
            {
                if(!image_uri.equals(image_uri_caller_table))

            {

                dataBase.updateCallerWhenContactInfoChanged(phoneNumber, callerName, image_uri);
            }
            }
            else
            {
                if(image_uri != image_uri_caller_table)
                    dataBase.updateCallerWhenContactInfoChanged(phoneNumber, callerName, image_uri);

            }

            if(callerName != null)
            {
                if ( !callerName.equals(callerName_caller_table)) {

                    dataBase.updateCallerWhenContactInfoChanged(phoneNumber, callerName, image_uri);


                }
            }
            else
            {

                if(callerName != callerName_caller_table)
                    dataBase.updateCallerWhenContactInfoChanged(phoneNumber, callerName, image_uri);

            }




        }



        CallRecordTrashDatabase trashDataBase = new CallRecordTrashDatabase(appContext);
        trashDataBase.openDatabase();
        Cursor trashCursor= trashDataBase.getCallerInfo();
        while(trashCursor.moveToNext())
        {

            image_uri_caller_table=trashCursor.getString(trashCursor.getColumnIndex("CONTACT_IMAGE_URI"));
            callerName_caller_table=trashCursor.getString(trashCursor.getColumnIndex("CALLER_NAME"));
            phoneNumber = trashCursor.getString(trashCursor.getColumnIndex("PHONE_NUMBER"));
            callerName = getNameFromContacts(phoneNumber);
            //trashDataBase.updateCallerWhenContactInfoChanged(phoneNumber, callerName, image_uri);

            if(image_uri != null )
            {
                if(!image_uri.equals(image_uri_caller_table))

            {

                trashDataBase.updateCallerWhenContactInfoChanged(phoneNumber, callerName, image_uri);
            }
            }
            else
            {
                if(image_uri != image_uri_caller_table)
                trashDataBase.updateCallerWhenContactInfoChanged(phoneNumber, callerName, image_uri);

            }

            if(callerName != null)
            {
                if ( !callerName.equals(callerName_caller_table)) {

                    trashDataBase.updateCallerWhenContactInfoChanged(phoneNumber, callerName, image_uri);


                }
            }
            else
            {

                if(callerName != callerName_caller_table)
                    trashDataBase.updateCallerWhenContactInfoChanged(phoneNumber, callerName, image_uri);

            }




        }




    }

    String getCountryISDcode() {

        String CountryID = "";
        String countryISDcode = "";

        TelephonyManager manager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = appContext.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                countryISDcode = g[0];
                break;
            }
        }

        return countryISDcode;

    }



    private String getNameFromContacts(String phoneNumber) {

        //System.out.println("Ramesh in GetNameFromContacts");
        String callerName = null;
        String phoneNumberFromContacts;
        String phoneNumberInInputFormat;
        image_uri=null;

        ContentResolver cr = appContext.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Photo.PHOTO_URI},
                //"ContactsContract.CommonDataKinds.Phone.CONTACT_ID =?",new String[]{phoneNumber},null);
                null, null, null);
        String ISD_code=getCountryISDcode();

        if (cursor != null) {


            while (cursor.moveToNext()) {
                phoneNumberFromContacts=cursor.getString(1);

                phoneNumberInInputFormat =
                        phoneNumberFromContacts.replace("(","").replace(")","").replace(" ","").replace("  ", "").replace("-", "");

                if(phoneNumberInInputFormat.substring(0,2).equals("00")   ) {

                    phoneNumberInInputFormat = "+" + phoneNumberInInputFormat.replaceFirst("00","");

                }
                else
                if(phoneNumberInInputFormat.substring(0,1).equals("0")) {
                    phoneNumberInInputFormat=phoneNumberInInputFormat.replaceFirst("0", "");

                }



                if(phoneNumber.substring(0,1).equals("0"))
                    phoneNumber=phoneNumber.replaceFirst("0","");



                if(phoneNumberInInputFormat.replace("+" + ISD_code,"").equals(phoneNumber.replace("+" + ISD_code,"")))
                {
                    callerName = cursor.getString(0);
                    image_uri= cursor.getString(2);

                }

            }


        }


        cursor.close();
        return callerName;
    }


}

