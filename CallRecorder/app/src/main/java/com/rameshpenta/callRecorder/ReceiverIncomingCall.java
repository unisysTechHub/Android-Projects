package com.rameshpenta.callRecorder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Handler;

public class ReceiverIncomingCall extends BroadcastReceiver {


    TelephonyManager telephonyManager;
    MediaRecorder mediaRecorder;

    File audioFile;
    SharedPreferences sp;
    AudioManager audioManager;
    SharedPreferences.Editor editor;
    Context context;
    String incomingNumber;
    boolean isRecording;
    long dateAsInteger;

    String callDate;
    String callTime;
    String callDateInFormat;


    public ReceiverIncomingCall() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        boolean outgoingCall = false;


        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);

        sp = context.getSharedPreferences("getFlags", Context.MODE_PRIVATE);
        editor = sp.edit();
        incomingNumber = intent.getStringExtra("incoming_number");
        System.out.println("Ramesh incoming NUmber :" + incomingNumber);
        //String flag = sp.getString("Flag", null);
        //System.out.println("Ramesh duplicate trigger flag :" + flag);
        boolean incomingCall = sp.getBoolean("RINGING", false);
        System.out.println("Ramesh RINGING Flag :" + incomingCall);
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        System.out.println("Ramesh call state :" + state);
        String outGoingCallNumber=null;


        /*if( !state.equals(savedState) && flag == null) {
            System.out.println("Ramesh flag null set");
            flag = "Y";

        }*/
        //
        if ( state.equals("OFFHOOK")  && !incomingCall)
        {
            outGoingCallNumber = intent.getStringExtra("incoming_number");
            System.out.println("Ramesh outgoing call ");
            outgoingCall = true;

                    }
        /*else
         if (flag == null)
        {

            editor.putString("Flag","Y");
            editor.commit();
        }*/


        int callState =0;

   //     if(flag == null)
     //   {


            if (state.equals("RINGING")) {


                editor.putBoolean("RINGING",true);
                editor.commit();

                callState=1;}
            else
            if(state.equals("OFFHOOK"))
            {


                if(!incomingCall)
                {
                    System.out.println("Ramesh not incoming call");
                    incomingNumber=null;
                }
                callState=2;

            }
            else
            if(state.equals("IDLE"))
            {
                editor.putBoolean("RINGING",false);
                editor.commit();
                callState=0;}


            if(incomingNumber == null)
                System.out.println("Ramesh incoming number null" + incomingNumber + "call State :" + callState);


            if(incomingNumber != null && !outgoingCall )
               onCallStateChanged(callState, incomingNumber);

            if(outgoingCall && outGoingCallNumber != null ) {
                incomingNumber=outGoingCallNumber;
                onCallStateChanged(callState, outGoingCallNumber);
                editor.putBoolean("IsRecording", false);
                editor.commit();

            }


 //       }

       /* else
        if (flag.equals("Y") )
        {

            editor.putString("Flag",null);
            editor.commit();


        }*/


    }


    public void onCallStateChanged(int state, String incomingNumber) {

        System.out.println("Ramesh Incoming Number :" + incomingNumber + "State :" + state);

        if (state == TelephonyManager.CALL_STATE_RINGING) {


        } else if (state == TelephonyManager.CALL_STATE_OFFHOOK ) {

            File callRecording=null;

            boolean SDStorage =PreferenceManager.getDefaultSharedPreferences(context).getBoolean("SD",false);

            if(SDStorage)
            {
                System.out.println("Ramesh SD Card memory");
                callRecording = new File(Environment.getExternalStorageDirectory(), "/CallRecords");

            }
            //boolean phoneStorage =PreferenceManager.getDefaultSharedPreferences(context).getBoolean("Phone",false);

            //if(phoneStorage)
            else
            {
                System.out.println("Ramesh Phone Memory");
                callRecording = new File(context.getFilesDir(), "/CallRecords");

            }


            if (!callRecording.exists()) {

                callRecording.mkdir();
            }

            Calendar calendar = Calendar.getInstance();
            Date currentTime =calendar.getTime();
            System.out.println("Ramesh current time " + currentTime);
            int length=currentTime.toString().length();

            String currentDate=currentTime.toString().replace(" ","").replace("+", "").replace(":", "");
            String audio_file_name = incomingNumber+currentDate.substring(0,8) +
                    currentTime.toString().substring(length-4,length)+
                    currentDate.substring(8,14)+
                    ".amr";

            System.out.println("Ramesh Current Date :" + currentDate);
            callDate = currentDate.substring(3,8)+currentTime.toString().substring(length-4,length);
            System.out.println("Ramesh call date :"+ callDate);

            callTime = currentDate.substring(8,10) + ":" + currentDate.substring(10,12);
            System.out.println("Ramesh call Time :" + callTime);

            String sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").format(currentTime);
            String dateInFormat=sd.replace("-","").replace("T","").replace(":", "").substring(0, 14);
             dateAsInteger= Long.parseLong(dateInFormat) ;
            System.out.println("Ramesh datAs Integer :" + dateAsInteger );
             callDateInFormat=sd.replace("-","").replace("T","").replace(":", "").substring(0, 8);
            System.out.println("Ramesh call Date in Format :" + callDateInFormat);

            System.out.println("Ramesh audio file name :" + audio_file_name);

            try {
                audioFile = new File(callRecording,audio_file_name);
                audioFile.createNewFile();
//                audioFile = File.createTempFile(audio_file_name, ".AMR", callRecording);

                System.out.println("Ramesh file name :"+ audioFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            insertCallTable();

            /*mediaRecorder = new MediaRecorder();

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());

            try {

                mediaRecorder.prepare();
                mediaRecorder.start();


                editor.putBoolean("IsRecording", true);
                editor.commit();

                insertCallTable();

            } catch (IOException e) {
                e.printStackTrace();
            }*/
            

        } else if (state == TelephonyManager.CALL_STATE_IDLE)
        {
            //IntentFilter intentFilter= new IntentFilter("callRecorder.App.Receive");
            //Intent intent=context.registerReceiver(null,intentFilter);
            Intent intent = new Intent(context,CallDatabaseUpdateService.class);
            System.out.println("Ramesh Call State Idle");

            if(intent != null) {

                System.out.println("Ramesh Stop Service");
                //Intent Sintent = new Intent(context,CallDatabaseUpdateService.class);
               // intent.setClass(context, CallDatabaseUpdateService.class);
                context.stopService(intent);

                            }


            isRecording = sp.getBoolean("IsRecording", false);

            if (isRecording)
            {

                System.out.println("Ramesh Media player stopped");
                //mediaRecorder.stop();
                editor.putBoolean("IsRecording", false);
                editor.commit();
            }


        }


    }


    void insertCallTable() {

        System.out.println("Ramesh Insert Call Table ");

        Intent Sintent = new Intent(context,CallDatabaseUpdateService.class);

        //Sintent.setAction("callRecorder.App.Receive");

        Sintent.putExtra("PhoneNumber", incomingNumber);
        Sintent.putExtra("FilePath", audioFile.getAbsolutePath());
        Sintent.putExtra("CALLDATE", callDateInFormat);
        Sintent.putExtra("CALLTIME", callTime);
        Sintent.putExtra("CALLDATEINFORMAT", dateAsInteger);

        //context.sendStickyBroadcast(Sintent);
        //editor.putBoolean("RINGING",false);
        //editor.commit();
        context.stopService(Sintent);
        context.startService(Sintent);



    }



}
