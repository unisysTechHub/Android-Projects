package com.rameshpenta.callRecorder;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.SyncStateContract;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Sujatha on 09-08-2015.
 */
public class MediaPlayerActivity {
    MediaPlayer mediaPlayer;
    Context context;
    AudioManager audioManager;
    SeekBar seekBar;
    AudioPlayThread audioPlayThread;
    TextView audioDuration;
    String saveFilePath=null;
    int draggedPosition=0;
    int lastStoppedosition;


    boolean stopActivePlayer=false;
    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;

    MediaPlayerActivity(Context context, View playerDialog)
    {
        this.context=context;
        seekBar=(SeekBar) playerDialog.findViewById(R.id.seekBar);
        audioDuration  = (TextView) playerDialog.findViewById(R.id.audioDuration);



    }


    public void StopActiveMusicPlayer()
    {
         stopActivePlayer=true;
        System.out.println("Ramesh stop active player flag set:");

    }

    public void playAudioFile(String filePath)  {
        System.out.println("Ramesh PlayAudioFile");
        
            saveFilePath=filePath;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(onAudioFocusChangeListener,
// Use the music stream.
                AudioManager.STREAM_MUSIC,
// Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
        System.out.println("Ramesh requestAudioFocus result :" + result);
        //System.out.println("Ramesh is Music Active " + audioManager.isMusicActive());

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {



            audioPlayThread= new AudioPlayThread();

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {

                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();



                int callDuration =mediaPlayer.getDuration() / 1000;
                // if( draggedPosition>0)
                //  mediaPlayer.seekTo( lastStoppedosition);
                String callDurationText = callDuration + "sec";
                if (callDuration > 60) {
                    int callDurationInMin = callDuration / 60;
                    callDurationText = callDurationInMin + "min";
                }

                seekBar.setMax(callDuration);
                lastStoppedosition = seekBar.getProgress();

                //System.out.println("Ramesh seekBar current Position " + lastStoppedosition);
                audioDuration.setText(callDurationText);


            } catch (IOException e) {
                e.printStackTrace();
            }

            audioPlayThread.execute(filePath, new Integer(0), null);

// other app had stopped playing song now , so u can do u stuff now .
        }

        onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                System.out.println("Ramesh focus Change :" + focusChange);
                switch (focusChange) {


                    case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) :
                        System.out.println("Ramesh aduiofucs transiet can duc");
                        // Lower the volume while ducking.
                        //audioManager.setV(0.2f, 0.2f);
                        break;
                    case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) :
                        System.out.println("Ramesh audo loss transient");
                        //mediaPlayer.pause();
                        break;

                    case (AudioManager.AUDIOFOCUS_LOSS) :
                        System.out.println("Ramesh aduifocus loss");
                        //mediaPlayer.stop();

                        break;

                    case (AudioManager.AUDIOFOCUS_GAIN) :
                        System.out.println("Ramesh audioFocus gain");
                        // Return the volume to normal and resume if paused.
                        //mediaPlayer.setVolume(1f, 1f);
                      //  mediaPlayer.start();
                        break;
                    default:
                    System.out.println("Ramesh audofoucs defualt");
                        break;
                }

            }
        };

    }

        class AudioPlayThread extends AsyncTask<Object,Integer,Object>
        {


            @Override
            protected void onPreExecute() {




            }

            @Override
            protected Object doInBackground(Object... params) {
                    System.out.println("Ramesh DoInBackground");

                mediaPlayer.start();

                if( lastStoppedosition > 0)
                {


                    mediaPlayer.seekTo(lastStoppedosition*1000);
                    //seekBar.setProgress(lastStoppedosition);
                }



                mediaPlayer.start();
               // mediaPlayer.start();




                    while (mediaPlayer.isPlaying()) {
                        publishProgress(new Integer(mediaPlayer.getCurrentPosition() / 1000));
                        if (stopActivePlayer)
                        {

                            break;
                    }
                   }


                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                seekBar.setProgress(values[0].intValue());

            }

            @Override
            protected void onPostExecute(Object o) {

                    mediaPlayer.stop();
                if(!stopActivePlayer)
                seekBar.setProgress(0);

                System.out.println("Ramesh Media Player Stopped");

                            }


        }

}
