package com.rameshpenta.callRecorder;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CallDatabaseUpdateService extends Service {
    Messenger callMessenger = new Messenger(new UpdateCallDatabase());
    Message message;
    boolean device_supported =false;
    CallRecordDataBase callRecordDataBase;
    MediaRecorder mediaRecorder;
    String image_uri = null;
    String ISD_code=null;
    public CallDatabaseUpdateService() {

    }




    class UpdateCallDatabase extends Handler {

        @Override
        public void handleMessage(Message msg) {
            System.out.println("Ramesh in handleMessage");

            switch (msg.what) {
                case 1:
                    boolean invalid_number=false;
                    Bundle bundle = msg.getData();
                    String phoneNumber = bundle.getString("PhoneNumber");
                    long  callDateInFormat =bundle.getLong("CALLDATEINFORMAT");

                     ISD_code = getCountryISDcode();
                    String callerName =getNameFromContacts(phoneNumber);
                    callRecordDataBase = new CallRecordDataBase(getApplicationContext());
                    if(phoneNumber.length() < 10   ) {

                        invalid_number = true;

                    }

                    else
                    if(phoneNumber.substring(0,1).equals("+") && phoneNumber.substring(1,2).equals("0")  ) {

                        invalid_number = true;

                    }
                    else

                    if(phoneNumber.substring(0,2).equals("00")   ) {

                        phoneNumber = "+" + phoneNumber.replaceFirst("00","");

                    }
                    else

                    if(!phoneNumber.substring(0,1).equals("+") && !phoneNumber.substring(0,1).equals("0")  ) {

                        phoneNumber = "+" +ISD_code + phoneNumber;

                    }
                    else
                    if(phoneNumber.substring(0,1).equals("0")  ) {

                        phoneNumber = "+"+ISD_code + phoneNumber.replaceFirst("0", "");
                    }



                    if(!invalid_number) {
                        updateCallerTable(phoneNumber, callerName, callDateInFormat);

                        String filePath = bundle.getString("FilePath");
                        String callDate = bundle.getString("CALLDATE");
                        String callTime = bundle.getString("CALLTIME");


                        updateCallRecordTable(phoneNumber, filePath, callDate, callTime, callDateInFormat,callerName);
                    }


            }


        }
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if(intent == null)
            System.out.println("Ramesh intent is null in service start command");
        System.out.println("Ramesh OnStartCommand");
        if(intent != null)
        {
            message = Message.obtain(null, 1);
            Bundle bundle = new Bundle();

            String phoneNumber = intent.getStringExtra("PhoneNumber");
            String filePath = intent.getStringExtra("FilePath");
            String callDate = intent.getStringExtra("CALLDATE");
            String calltime = intent.getStringExtra("CALLTIME");
            long dateAsInt   = intent.getLongExtra("CALLDATEINFORMAT",0);
            bundle.putString("PhoneNumber", phoneNumber);
            bundle.putString("FilePath", filePath);
            bundle.putString("CALLDATE", callDate);
            bundle.putString("CALLTIME", calltime);
            bundle.putLong("CALLDATEINFORMAT", dateAsInt);
            message.setData(bundle);
            mediaRecorder = new MediaRecorder();

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            // mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(filePath);

            try {

                mediaRecorder.prepare();
                device_supported=true;
                mediaRecorder.start();
                //Toast.makeText(this,"Call Recording is blocked in Your Mobile ",Toast.LENGTH_LONG).show();

            } catch (Exception e) {

                e.printStackTrace();

                Toast.makeText(this,"Call Recording is blocked in Your Mobile - Records only your voice ",Toast.LENGTH_LONG).show();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                try {

                    mediaRecorder.prepare();
                    device_supported=true;
                    mediaRecorder.start();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    device_supported=false;
                    Toast.makeText(this,"Recording your voice feature is not available  in your mobile ",Toast.LENGTH_LONG).show();
                }

            }
            /*try {
                callMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return callMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        System.out.println("Ramesh Service On Destroy");

        if(device_supported)
        {
            if (mediaRecorder != null) {

                mediaRecorder.stop();
                try {
                    callMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        super.onDestroy();
    }

    String getCountryISDcode() {

        String CountryID = "";
        String countryISDcode = "";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
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

        System.out.println("Ramesh in GetNameFromContacts");
        String callerName = null;
        String phoneNumberFromContacts;
        String phoneNumberInInputFormat;
        image_uri=null;

        ContentResolver cr = getApplicationContext().getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Photo.PHOTO_URI},
                //"ContactsContract.CommonDataKinds.Phone.CONTACT_ID =?",new String[]{phoneNumber},null);
                null, null, null);

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


        System.out.println("Ramesh caller name :" + callerName);
        cursor.close();
        return callerName;
    }

    private  void updateCallerTable(String phoneNumber,String callerName,long dateInFormat)
    {
        System.out.println("Ramesh in UpdateCaller database");
        callRecordDataBase.openDatabase();


        if(callRecordDataBase.isCallingFirstTime(phoneNumber))
        {

            callRecordDataBase.insertCALLER(phoneNumber,callerName,image_uri,dateInFormat);


        }
    }

    private void updateCallRecordTable(String phoneNumber,String filePath,String callDate,String callTIme,long dateInFormat,String callerName)
    {

        System.out.println("Ramesh in UpdateCallRecord Table");
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }

        int currentDuration =mediaPlayer.getDuration();
        callRecordDataBase.insertCallRecord(phoneNumber, filePath, callDate, callTIme,dateInFormat,currentDuration);
        callRecordDataBase.insertCallRecordForMonthlyReport(phoneNumber, filePath, callDate, callTIme, dateInFormat, currentDuration,callerName);
        mediaPlayer.release();



    }



}
