package com.rameshpenta.callRecorder;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorTreeAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Sujatha on 14-08-2015.
 */
public class ExpandableListAdapterSimpleCursorByDate extends CursorTreeAdapter {
    CallRecordDataBase dataBase=null;
    CallRecordTrashDatabase trashDatabase=null;
    MainActivity callRecordDatabaseCursorLoader;
    LoaderManager loaderManager;
    Cursor callerRecordTable;

    Context context;

    MediaPlayerActivity mediaPlayerActivity;

    View firstRow;
    //View secondRow;
    View playerDialog;
    AudioManager audioManager;
    int draggedPosition=0;
    View saveView;
    String filePathMediaFile;
    String dateOfCall;
    boolean sortByDate;


    ArrayList<String> checkedForDelete= new ArrayList<String>();
    ArrayList<String> checkedForGroupDelete= new ArrayList<String>();




    public ExpandableListAdapterSimpleCursorByDate(Cursor cursor,Context context, Object dataBase,boolean sortByDate) {
        super(cursor,context);

        this.sortByDate=sortByDate;
        if(dataBase.getClass().toString().contains("CallRecordDataBase"))
            this.dataBase= (CallRecordDataBase) dataBase;
        else
        if(dataBase.getClass().toString().contains("CallRecordTrashDatabase"))
            this.trashDatabase= (CallRecordTrashDatabase) dataBase;
        this.context=context;

    }
    class ViewHolderGroupItemByDate
    {

        TextView callDate;
        CheckBox checkBox;
        TextView dailyCallDuration;


        ViewHolderGroupItemByDate(View groupItemLayout)
        {

            callDate = (TextView) groupItemLayout.findViewById(R.id.call_date_group_item);

            checkBox= (CheckBox) groupItemLayout.findViewById(R.id.call_date_group_item_checkbox);
            dailyCallDuration= (TextView) groupItemLayout.findViewById(R.id.dailyCallDuration);



        }


    }


    @Override
    public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {

        View groupItemLayout = LayoutInflater.from(context).inflate(R.layout.group_view_list_item_bydate, null);


        return groupItemLayout;
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {

        ViewHolderGroupItemByDate viewHolderGroupItem = (ViewHolderGroupItemByDate) view.getTag();
        if (viewHolderGroupItem != null)
            ;
        else {
            viewHolderGroupItem = new ViewHolderGroupItemByDate(view);
            view.setTag(viewHolderGroupItem);
            //callerTable.moveToFirst();

        }


        //                System.out.println("Ramesh in cursor");
        //System.out.println("Ramesh boolean :" + sortByDate);
        if(sortByDate) {
            viewHolderGroupItem.callDate.setText(cursor.getString(cursor.getColumnIndex("CALL_DATE")));
            int callDuration = 0;
            String callDurationText = "";
            callDuration = cursor.getInt(cursor.getColumnIndex("SUM(CALL_DURATION)"));

            callDuration = callDuration / 1000;
            callDurationText = callDuration + "sec";
            if (callDuration > 60) {
                callDuration = callDuration / 60;
                callDurationText = callDuration + "min";
            }

            viewHolderGroupItem.dailyCallDuration.setText(callDurationText);

        }
        else {

            viewHolderGroupItem.callDate.setText(cursor.getString(cursor.getColumnIndex("CALL_MONTH")));
        }

        viewHolderGroupItem.checkBox.setVisibility(View.VISIBLE);


        if (checkedForGroupDelete.size() > 0)
        {

            if(checkedForGroupDelete.contains(cursor.getString(cursor.getColumnIndex("CALL_DATE"))))
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
                checkedForGroupDelete.remove(cursor.getString(cursor.getColumnIndex("CALL_DATE")));

            }
            viewHolderGroupItem.checkBox.setVisibility(View.GONE);

        }

        viewHolderGroupItem.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked())
                {
                    LinearLayout linearLayout = (LinearLayout) v.getParent();

                    TextView callDate = (TextView) linearLayout.findViewById(R.id.call_date_group_item);
                    checkedForGroupDelete.add(callDate.getText().toString());
                }
                else
                if (!((CheckBox)v).isChecked())
                {
                    LinearLayout linearLayout = (LinearLayout) v.getParent();

                    TextView callDate = (TextView) linearLayout.findViewById(R.id.call_date_group_item);
                    checkedForGroupDelete.remove(callDate.getText().toString());
                }




            }
        });




        cursor.moveToNext();



    }



    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor)
    {
        String callDate;
        if(sortByDate)
        callDate =groupCursor.getString(groupCursor.getColumnIndex("CALL_DATE"));
        else {
            callDate = groupCursor.getString(groupCursor.getColumnIndex("CALL_MONTH"));
        }

        if(dataBase!=null) {
            if(sortByDate)
            callerRecordTable = dataBase.getCallerRecordsByDate(callDate);
            else
                callerRecordTable = dataBase.getCallerRecordsByMonth(callDate);
        }
        else
        if(trashDatabase!=null)
        {  if(sortByDate)
            callerRecordTable = trashDatabase.getCallerRecordsByDate(callDate);
            else
            callerRecordTable = trashDatabase.getCallerRecordsByMonth(callDate);
        }


        return callerRecordTable;
    }


    @Override
    public View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {

        View childItemLayout = LayoutInflater.from(context).inflate(R.layout.inbox_list_item_expanded, null);

        return childItemLayout;
    }



    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        //System.out.println("Ramesh bind child View :" + view);
        ViewHolderChildItem viewHolderChildItem = (ViewHolderChildItem) view.getTag();
        if (viewHolderChildItem != null) ;

        else {
            viewHolderChildItem = new ViewHolderChildItem(view);
            view.setTag(viewHolderChildItem);
            //System.out.println("Ramesh caller records cursor move to First");
            // cursor.moveToFirst();

        }

        Cursor callerTable=null;
        int callDuration = 0;
        String callDurationText = "";
        String callerName;
        callDuration = cursor.getInt(cursor.getColumnIndex("CALL_DURATION"));

        callDuration = callDuration / 1000;
        callDurationText = callDuration + "sec";
        if (callDuration > 60) {
            callDuration = callDuration / 60;
            callDurationText = callDuration + "min";
        }

        String callDateInFormat=cursor.getString(cursor.getColumnIndex("CALL_DATE"));
        String callDateMMM=Utils.getMonthName(Integer.parseInt(callDateInFormat.substring(4, 6)));
        String callDateMMMDDYYYY= callDateMMM+ callDateInFormat.substring(6,8)+"\n"+callDateInFormat.substring(0,4);

        String phoneNumber = cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"));

        if(sortByDate) {
            if (dataBase != null)
                callerTable = dataBase.getCallerInfoByPhoneNumber(phoneNumber);
            else if (trashDatabase != null)
                callerTable = trashDatabase.getCallerInfoByPhoneNumber(phoneNumber);
            callerTable.moveToNext();

             callerName = callerTable.getString(callerTable.getColumnIndex("CALLER_NAME"));
            callerTable.close();
        }
        else
        {
             callerName = cursor.getString(cursor.getColumnIndex("CALLER_NAME"));

        }
        //String callerName=null;

        if(callerName != null)
        {
            viewHolderChildItem.callerName.setVisibility(View.VISIBLE);
            viewHolderChildItem.callerName.setText(callerName+" ");
            viewHolderChildItem.phoneNumber.setVisibility(View.GONE);

        }
        else {
            viewHolderChildItem.phoneNumber.setVisibility(View.VISIBLE);
            viewHolderChildItem.callerName.setVisibility(View.GONE);



        }


        viewHolderChildItem.phoneNumber.setText(phoneNumber + " ");
        viewHolderChildItem.filePath.setText(cursor.getString(cursor.getColumnIndex("FILE_PATH")));

        viewHolderChildItem.callDate.setText(callDateMMMDDYYYY);
        viewHolderChildItem.callTime.setText(cursor.getString(cursor.getColumnIndex("CALL_TIME")));
        viewHolderChildItem.duration.setText(callDurationText);
        if ( filePathMediaFile != null)
        {

            if (filePathMediaFile.equals(cursor.getString(cursor.getColumnIndex("FILE_PATH"))))
            {
                viewHolderChildItem.playerDialog.setVisibility(View.VISIBLE);
                //viewHolderChildItem.secondRow.setVisibility(View.GONE);

            }
            else {
                viewHolderChildItem.playerDialog.setVisibility(View.GONE);
                //viewHolderChildItem.secondRow.setVisibility(View.VISIBLE);
            }


        }
        else {
            viewHolderChildItem.playerDialog.setVisibility(View.GONE);
          //  viewHolderChildItem.secondRow.setVisibility(View.VISIBLE);
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

        viewHolderChildItem.functionCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout linearLayout = (LinearLayout) v.getParent().getParent();
                firstRow = linearLayout.findViewById(R.id.firstRowLayout);
                TextView phoneNumber = (TextView) firstRow.findViewById(R.id.phone_number);
                String callerNumber=phoneNumber.getText().toString();
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + callerNumber));
                ExpandableListAdapterSimpleCursorByDate.this.context.startActivity(intent);

            }
        });
        cursor.moveToNext();



    }

    public void displayMediaPlayer(View view)

    {
        saveView = view;

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        System.out.println("Ramesh isMusicplayer Active :" + audioManager.isMusicActive());

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
          //  firstRow.setBackgroundColor(context.getResources().getColor(R.color.white));
            firstRow.setVisibility(View.VISIBLE);}

        //if(secondRow != null)
           // secondRow.setVisibility(View.VISIBLE);




        System.out.println("Ramesh displayMediaPlayer function");

        View view1 = (View) view.getParent().getParent();

        LinearLayout linearLayout = (LinearLayout)view1.getParent();
        playerDialog = linearLayout.findViewById(R.id.player_Dialog);
        firstRow    =  linearLayout.findViewById(R.id.firstRowLayout);

        TextView filePath = (TextView) firstRow.findViewById(R.id.file_path);
        TextView callDate = (TextView) firstRow.findViewById(R.id.call_date);
        dateOfCall=callDate.getText().toString().trim();
        filePathMediaFile = filePath.getText().toString();
        System.out.println("Ramesh filePath :" + filePathMediaFile);


        mediaPlayerActivity = new MediaPlayerActivity(context,playerDialog);


        // if(draggedPosition>0)
        //   mediaPlayerActivity.draggedPosition=draggedPosition;

        mediaPlayerActivity.playAudioFile(filePathMediaFile);

        //firstRow.setBackgroundColor(context.getResources().getColor(R.color.holo_blue_bright));
       // secondRow    =linearLayout.findViewById(R.id.secondRowLayout);

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
        TextView phoneNumber;
        CheckBox checkBox;
        ImageView functionCall;
        TextView duration;
        View playerDialog;
        TextView callerName;
       // View secondRow;









        ViewHolderChildItem(View viewHolderChildItem) {

            seekBar = (SeekBar) viewHolderChildItem.findViewById(R.id.seekBar);
            phoneNumber= (TextView) viewHolderChildItem.findViewById(R.id.phone_number);
            filePath = (TextView) viewHolderChildItem.findViewById(R.id.file_path);
            callDate = (TextView) viewHolderChildItem.findViewById(R.id.call_date);
            callTime = (TextView) viewHolderChildItem.findViewById(R.id.call_time);
            TranscriptedText = (TextView) viewHolderChildItem.findViewById(R.id.transcribedText);
            functionPlay = (ImageView) viewHolderChildItem.findViewById(R.id.functionPlay);
            pauseButton = (ImageView) viewHolderChildItem.findViewById(R.id.playPauseBtn);
            stopButton = (ImageView) viewHolderChildItem.findViewById(R.id.playstopButton);
            checkBox = (CheckBox) viewHolderChildItem.findViewById(R.id.checkedForDelete);
            functionCall= (ImageView) viewHolderChildItem.findViewById(R.id.functionCall);
            duration= (TextView) viewHolderChildItem.findViewById(R.id.duration);
            playerDialog=viewHolderChildItem.findViewById(R.id.player_Dialog);
            callerName= (TextView) viewHolderChildItem.findViewById(R.id.caller_name);
         //   secondRow=viewHolderChildItem.findViewById(R.id.secondRowLayout);


            phoneNumber.setVisibility(View.VISIBLE);




        }
    }
}
