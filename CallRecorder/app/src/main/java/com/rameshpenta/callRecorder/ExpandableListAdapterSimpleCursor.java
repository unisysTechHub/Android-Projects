package com.rameshpenta.callRecorder;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorTreeAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by Sujatha on 13-08-2015.
 */

public class ExpandableListAdapterSimpleCursor extends CursorTreeAdapter {

    CallRecordDataBase dataBase=null;
    CallRecordTrashDatabase trashDatabase=null;
    MainActivity callRecordDatabaseCursorLoader;
    LoaderManager loaderManager;
    Cursor callerRecordTable;
    Context context;

    MediaPlayerActivity mediaPlayerActivity;
    View firstRow;
   // View secondRow;


    View playerDialog;
    ArrayList<String> checkedForDelete= new ArrayList<String>();
    AudioManager audioManager;
    int draggedPosition=0;
    View saveView;
    String filePathMediaFile;
    String phoneNumber;

    ArrayList<String> checkedForGroupDelete= new ArrayList<String>();


    public ExpandableListAdapterSimpleCursor( Cursor cursor,Context context, Object dataBase) {
        super( cursor, context);

        if(dataBase.getClass().toString().contains("CallRecordDataBase"))
        this.dataBase= (CallRecordDataBase) dataBase;
        else
        if(dataBase.getClass().toString().contains("CallRecordTrashDatabase"))
            this.trashDatabase= (CallRecordTrashDatabase) dataBase;
        this.context=context;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {

        String phoneNumber =groupCursor.getString(groupCursor.getColumnIndex("PHONE_NUMBER"));
        if(dataBase!=null)
        {

        callerRecordTable=dataBase.getCallerRecords(phoneNumber);


        }
        else
        if(trashDatabase!=null)
             callerRecordTable=trashDatabase.getCallerRecords(phoneNumber);




        return callerRecordTable;

    }
    class ViewHolderGroupItem
    {
        ImageView contactImage;
        TextView contactName;
        TextView contactNumber;
        TextView noOfCalls;
        CheckBox checkBox;
        TextView callDurationByPhoneNumber;


        ViewHolderGroupItem(View groupItemLayout)
        {
            contactImage = (ImageView) groupItemLayout.findViewById(R.id.contact_image);
            contactName = (TextView) groupItemLayout.findViewById(R.id.contact_name);
            contactNumber = (TextView) groupItemLayout.findViewById(R.id.contact_number);
            noOfCalls = (TextView) groupItemLayout.findViewById(R.id.no_of_calls);
            checkBox= (CheckBox) groupItemLayout.findViewById(R.id.groupItem_checkBox);
            callDurationByPhoneNumber= (TextView) groupItemLayout.findViewById(R.id.callDurationByPhoneNumber);


        }


    }

    @Override
    public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {


        View view = LayoutInflater.from(context).inflate(R.layout.group_view_list_item, null);



        return view;

    }


    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded)
    {



        ViewHolderGroupItem viewHolderGroupItem = (ViewHolderGroupItem) view.getTag();
        if (viewHolderGroupItem != null)
            ;
        else {
            viewHolderGroupItem = new ViewHolderGroupItem(view);
            view.setTag(viewHolderGroupItem);
            //callerTable.moveToFirst();

        }
            String image_Uri = cursor.getString(cursor.getColumnIndex("CONTACT_IMAGE_URI"));
            if(image_Uri !=null)
            viewHolderGroupItem.contactImage.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex("CONTACT_IMAGE_URI"))));
            else
            viewHolderGroupItem.contactImage.setImageResource(R.drawable.ic_identity);
            viewHolderGroupItem.contactNumber.setText(cursor.getString(cursor.getColumnIndex("PHONE_NUMBER")));
            viewHolderGroupItem.contactName.setText(cursor.getString(cursor.getColumnIndex("CALLER_NAME")));
            //viewHolderGroupItem.noOfCalls.setText(cursor.getString(cursor.getColumnIndex("NUMBER_OF_CALLS")));
            if(dataBase!=null)
            viewHolderGroupItem.noOfCalls.setText( "" +cursor.getInt(6));
            else
                viewHolderGroupItem.noOfCalls.setText(cursor.getString(cursor.getColumnIndex("NUMBER_OF_CALLS")));

            viewHolderGroupItem.checkBox.setVisibility(View.VISIBLE);

        int callDuration = 0;
        String callDurationText = "";

        callDuration = cursor.getInt(5);

        callDuration = callDuration / 1000;
        callDurationText = callDuration + "sec";
        if (callDuration > 60) {
            callDuration = callDuration / 60;
            callDurationText = callDuration + "min";
        }

        viewHolderGroupItem.callDurationByPhoneNumber.setText(callDurationText);

        if (checkedForGroupDelete.size() > 0)
        {

            if(checkedForGroupDelete.contains(cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"))))
                viewHolderGroupItem.checkBox.setChecked(true);
            else
                viewHolderGroupItem.checkBox.setChecked(false);

        }
        else
            viewHolderGroupItem.checkBox.setChecked(false);

        if(isExpanded)
        {

            if(viewHolderGroupItem.checkBox.isChecked())
            {
                viewHolderGroupItem.checkBox.setChecked(false);
                checkedForGroupDelete.remove(cursor.getString(cursor.getColumnIndex("PHONE_NUMBER")));

            }
            viewHolderGroupItem.checkBox.setVisibility(View.INVISIBLE);

        }

        viewHolderGroupItem.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    LinearLayout linearLayout = (LinearLayout) v.getParent();


                    TextView contactNumber = (TextView) linearLayout.findViewById(R.id.contact_number);

                    checkedForGroupDelete.add(contactNumber.getText().toString());
                } else if (!((CheckBox) v).isChecked()) {
                    LinearLayout linearLayout = (LinearLayout) v.getParent();

                    TextView contactNumber = (TextView) linearLayout.findViewById(R.id.contact_number);
                    checkedForGroupDelete.remove(contactNumber.getText().toString());
                }


            }
        });



        cursor.moveToNext();
    }




        // cursor.moveToLast();


    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {

        View childItemLayout = LayoutInflater.from(context).inflate(R.layout.inbox_list_item_expanded, null);
        return childItemLayout;
    }



    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {

        ViewHolderChildItem viewHolderChildItem = (ViewHolderChildItem) view.getTag();
        if (viewHolderChildItem != null) ;


        else {
            viewHolderChildItem = new ViewHolderChildItem(view);
            view.setTag(viewHolderChildItem);

            // cursor.moveToFirst();

        }


        int callDuration = 0;
        String callDurationText = "";

         callDuration = cursor.getInt(cursor.getColumnIndex("CALL_DURATION"));

            callDuration = callDuration / 1000;
            callDurationText = callDuration + "sec";
            if (callDuration > 60) {
                callDuration = callDuration / 60;
                callDurationText = callDuration + "min";
            }


        String callDateInFormat=cursor.getString(cursor.getColumnIndex("CALL_DATE"));

        String callDateMMM=Utils.getMonthName(Integer.parseInt(callDateInFormat.substring(4, 6)));
        String callDateMMMDDYYYY= callDateMMM+ callDateInFormat.substring(6,8)+callDateInFormat.substring(0,4);


        viewHolderChildItem.phoneNumber.setText(cursor.getString(cursor.getColumnIndex("PHONE_NUMBER")) + "  ");
        viewHolderChildItem.filePath.setText(cursor.getString(cursor.getColumnIndex("FILE_PATH")));
        viewHolderChildItem.callDate.setText(callDateMMMDDYYYY);
        viewHolderChildItem.callTime.setText(cursor.getString(cursor.getColumnIndex("CALL_TIME")));
        viewHolderChildItem.duration.setText(callDurationText);
        if ( filePathMediaFile != null)
        {

        if (filePathMediaFile.equals(cursor.getString(cursor.getColumnIndex("FILE_PATH"))))
        {
            viewHolderChildItem.playerDialog.setVisibility(View.VISIBLE);
          //  viewHolderChildItem.secondRow.setVisibility(View.GONE);

        }
        else {
            viewHolderChildItem.playerDialog.setVisibility(View.GONE);
            //viewHolderChildItem.secondRow.setVisibility(View.VISIBLE);
        }


          }
        else {
            viewHolderChildItem.playerDialog.setVisibility(View.GONE);
        //    viewHolderChildItem.secondRow.setVisibility(View.VISIBLE);
        }


        if (checkedForDelete.size() > 0)
        {
            if(checkedForDelete.contains(cursor.getString(cursor.getColumnIndex("FILE_PATH"))))
                viewHolderChildItem.checkBox.setChecked(true);
            else
                viewHolderChildItem.checkBox.setChecked(false);

        }
        else
            viewHolderChildItem.checkBox.setChecked(false);

        viewHolderChildItem.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                displayMediaPlayer((View) seekBar);

            }
        });

        viewHolderChildItem.functionPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 100);


                System.out.println("Ramesh function play Clicked");

                            displayMediaPlayer(v);



            }
        });
        viewHolderChildItem.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Ramesh stop Button pressed");

                if( audioManager.isMusicActive() && mediaPlayerActivity != null)
                {
                    mediaPlayerActivity.StopActiveMusicPlayer();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 100);

                    System.out.println("Ramesh stop active Music Player");

                }


            }
        });


        viewHolderChildItem.pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Ramesh pauseButton Pressed");

                if (audioManager.isMusicActive() && mediaPlayerActivity != null) {
                    mediaPlayerActivity.StopActiveMusicPlayer();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 100);

                    System.out.println("Ramesh stop active Music Player");

                }


            }
        });

        viewHolderChildItem.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked())
                {
                    LinearLayout linearLayout = (LinearLayout) v.getParent();
                    firstRow = linearLayout.findViewById(R.id.firstRowLayout);
                    TextView filePath = (TextView) firstRow.findViewById(R.id.file_path);
                    checkedForDelete.add(filePath.getText().toString());
                }
                else
                if (!((CheckBox)v).isChecked())
                {
                    LinearLayout linearLayout = (LinearLayout) v.getParent();
                    firstRow = linearLayout.findViewById(R.id.firstRowLayout);
                    TextView filePath = (TextView) firstRow.findViewById(R.id.file_path);
                    checkedForDelete.remove(filePath.getText().toString());
                }


            }
        });

        viewHolderChildItem.functincall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                  LinearLayout linearLayout = (LinearLayout) v.getParent().getParent();
                  firstRow = linearLayout.findViewById(R.id.firstRowLayout);
                  TextView phoneNumber = (TextView) firstRow.findViewById(R.id.phone_number);
                  String callerNumber = phoneNumber.getText().toString();
                  Intent intent = new Intent(Intent.ACTION_CALL);
                  intent.setData(Uri.parse("tel:" + callerNumber));
                  ExpandableListAdapterSimpleCursor.this.context.startActivity(intent);



            }
        });
        cursor.moveToNext();


    }
    public void displayMediaPlayer(View view)

    {
        System.out.println("Ramesh displayMediaPlayer function");
        saveView = view;

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);


        if( audioManager.isMusicActive() && mediaPlayerActivity != null)
        {


            mediaPlayerActivity.StopActiveMusicPlayer();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                }
            }, 100);
            System.out.println("Ramesh stop active Music Player");

        }

        if(playerDialog != null)
        {

            playerDialog.setVisibility(View.GONE);

        }
        if(firstRow !=null)
        {
            //firstRow.setBackgroundColor(context.getResources().getColor(R.color.white));
            firstRow.setVisibility(View.VISIBLE);}

       // if(secondRow != null)
         //   secondRow.setVisibility(View.VISIBLE);





        View view1 = (View) view.getParent().getParent();

        LinearLayout linearLayout = (LinearLayout)view1.getParent();
        playerDialog = linearLayout.findViewById(R.id.player_Dialog);
        firstRow    =  linearLayout.findViewById(R.id.firstRowLayout);
        TextView phoneNo = (TextView) firstRow.findViewById(R.id.phone_number);
        phoneNumber=phoneNo.getText().toString().trim();
        TextView filePath = (TextView) firstRow.findViewById(R.id.file_path);
         filePathMediaFile = filePath.getText().toString();
       // System.out.println("Ramesh filePath :" + filePathMediaFile);


        mediaPlayerActivity = new MediaPlayerActivity(context,playerDialog);



        // if(draggedPosition>0)
        //   mediaPlayerActivity.draggedPosition=draggedPosition;

        mediaPlayerActivity.playAudioFile(filePathMediaFile);

        //firstRow.setBackgroundColor(context.getResources().getColor(R.color.holo_blue_bright));
      //  secondRow    =linearLayout.findViewById(R.id.secondRowLayout);

        //secondRow.setVisibility(View.GONE);
        //view.setVisibility(View.GONE);
        playerDialog.setVisibility(View.VISIBLE);
        draggedPosition=0;






    }

    class ViewHolderChildItem {
        TextView callDate;
        TextView callTime;
        TextView TranscriptedText;
        TextView filePath;
        ImageView functionPlay;
        ImageView pauseButton;
        ImageView stopButton;
        SeekBar seekBar;
        CheckBox checkBox;
        TextView phoneNumber;
        TextView duration;
        ImageView functincall;
        View playerDialog;
       // View secondRow;

        ViewHolderChildItem(View viewHolderChildItem) {
            seekBar = (SeekBar) viewHolderChildItem.findViewById(R.id.seekBar);
            filePath = (TextView) viewHolderChildItem.findViewById(R.id.file_path);
            callDate = (TextView) viewHolderChildItem.findViewById(R.id.call_date);
            callTime = (TextView) viewHolderChildItem.findViewById(R.id.call_time);
            TranscriptedText = (TextView) viewHolderChildItem.findViewById(R.id.transcribedText);
            functionPlay = (ImageView) viewHolderChildItem.findViewById(R.id.functionPlay);
            pauseButton = (ImageView) viewHolderChildItem.findViewById(R.id.playPauseBtn);
            stopButton = (ImageView) viewHolderChildItem.findViewById(R.id.playstopButton);
            checkBox = (CheckBox) viewHolderChildItem.findViewById(R.id.checkedForDelete);
            duration = (TextView) viewHolderChildItem.findViewById(R.id.duration);
            phoneNumber= (TextView) viewHolderChildItem.findViewById(R.id.phone_number);
            functincall= (ImageView) viewHolderChildItem.findViewById(R.id.functionCall);
            playerDialog=viewHolderChildItem.findViewById(R.id.player_Dialog);
         //   secondRow=viewHolderChildItem.findViewById(R.id.secondRowLayout);

        }
    }

}
