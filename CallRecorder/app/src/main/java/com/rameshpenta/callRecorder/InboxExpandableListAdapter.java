package com.rameshpenta.callRecorder;


import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.DragEvent;
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

/**
 * Created by Sujatha on 09-08-2015.
 */


class InboxExpandableListAdapter extends CursorTreeAdapter
{
        int groupCount=1;
        CallRecordDataBase dataBase;
        MainActivity callRecordDatabaseCursorLoader;
        LoaderManager loaderManager;
        Cursor callerRecordTable;

        Context context;
        MediaPlayerActivity mediaPlayerActivity;
        View firstRow;
        View secondRow;
        View playerDialog;
        AudioManager audioManager;
        int draggedPosition=0;
        View saveView;
        Bundle bundle = new Bundle();
        ContentResolver cr;
        Cursor cursor;
        boolean groupOnClickListener =false;


    //public InboxExpandableListAdapter(Cursor cursor, Context context,CallRecordDataBase dataBase) {
    public InboxExpandableListAdapter(Cursor cursor, Context context,CallRecordDataBase dataBase) {
        super(cursor, context);
        this.dataBase=dataBase;
        this.context=context;
        this.cursor=cursor;
        cr =context.getContentResolver();


    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {

        System.out.println("Ramesh get ChildrenCurosr");
        String phoneNumber =groupCursor.getString(groupCursor.getColumnIndex("PHONE_NUMBER"));

        callerRecordTable=dataBase.getCallerRecords(phoneNumber);

        return callerRecordTable;
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        System.out.println("Ramesh Cursor newGroupView " + cursor.getCount());

        View view = LayoutInflater.from(context).inflate(R.layout.group_view_list_item, null);



        return view;

    }


    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {

        System.out.println("Ramesh  bindGroupView tset" + cursor.getCount());

            if( !groupOnClickListener) {
                ViewHolderGroupItem viewHolderGroupItem = (ViewHolderGroupItem) view.getTag();
                if (viewHolderGroupItem != null)
                    ;
                else {
                    viewHolderGroupItem = new ViewHolderGroupItem(view);
                    view.setTag(viewHolderGroupItem);
                    System.out.println("Rames groupitem move to first");
                    //callerTable.moveToFirst();

                }


                String image_uri = null;
                String phoneNumberFromContacts;
                String phoneNumberInInputFormat;


                String phoneNumber = cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"));
                System.out.println("Ramesh phone Number from Database :" + phoneNumber);
                Cursor cursorContacts = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Photo.PHOTO_URI},
                        //"ContactsContract.CommonDataKinds.Phone.CONTACT_ID =?",new String[]{phoneNumber},null);
                        null, null, null);
                if (cursorContacts != null) {


                    while (cursorContacts.moveToNext()) {
                        phoneNumberFromContacts = cursorContacts.getString(0);
                        System.out.println("Ramesh phone numbers from contac ts :" + phoneNumberFromContacts);
                        phoneNumberInInputFormat =
                                phoneNumberFromContacts.replace("(", "").replace(")", "").replace(" ", "").replace("  ", "").replace("-", "");

                        if (phoneNumberInInputFormat.equals(phoneNumber)) {
                            image_uri = cursorContacts.getString(1);
                            System.out.println("Ramesh image Uri " + image_uri);
                        }

                    }


                }


                //                System.out.println("Ramesh in cursor");

                if (image_uri != null)
                    viewHolderGroupItem.contactImage.setImageURI(Uri.parse(image_uri));
                viewHolderGroupItem.contactName.setText(cursor.getString(cursor.getColumnIndex("PHONE_NUMBER")));
                viewHolderGroupItem.contactNumber.setText(cursor.getString(cursor.getColumnIndex("CALLER_NAME")));
                viewHolderGroupItem.noOfCalls.setText(cursor.getString(cursor.getColumnIndex("NUMBER_OF_CALLS")));
            }




       // cursor.moveToLast();



    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {

        View childItemLayout = LayoutInflater.from(context).inflate(R.layout.inbox_list_item_expanded, null);
        CheckBox checkBox = (CheckBox) childItemLayout.findViewById(R.id.checkedForDelete);
        checkBox.setVisibility(View.VISIBLE);
        System.out.println("Ramesh newCHildVIew");
        return childItemLayout;
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {

        System.out.println("Ramesh bindCHildView ");
        ViewHolderChildItem viewHolderChildItem=(ViewHolderChildItem)view.getTag();
        if (viewHolderChildItem != null);

        else
        {
            viewHolderChildItem = new ViewHolderChildItem(view);
            view.setTag(viewHolderChildItem);
            System.out.println("Ramesh caller records cursor move to First");
           // cursor.moveToFirst();

        }



        viewHolderChildItem.filePath.setText(cursor.getString(cursor.getColumnIndex("FILE_PATH")));
        viewHolderChildItem.callDate.setText(cursor.getString(cursor.getColumnIndex("CALL_DATE")));
        viewHolderChildItem.callTime.setText(cursor.getString(cursor.getColumnIndex("CALL_TIME")));

        viewHolderChildItem.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                displayMediaPlayer((View)seekBar);

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
                displayMediaPlayer( v);
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

        cursor.moveToNext();


    }

    class ViewHolderGroupItem
    {
        ImageView contactImage;
        TextView contactName;
        TextView contactNumber;
        TextView noOfCalls;


        ViewHolderGroupItem(View groupItemLayout)
        {
            contactImage = (ImageView) groupItemLayout.findViewById(R.id.contact_image);
            contactName = (TextView) groupItemLayout.findViewById(R.id.contact_name);
            contactNumber = (TextView) groupItemLayout.findViewById(R.id.contact_number);
            noOfCalls = (TextView) groupItemLayout.findViewById(R.id.no_of_calls);




        }


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
            firstRow.setBackgroundColor(context.getResources().getColor(R.color.white));
            firstRow.setVisibility(View.VISIBLE);}

        if(secondRow != null)
            secondRow.setVisibility(View.VISIBLE);



        System.out.println("Ramesh displayMediaPlayer function");

        View view1 = (View) view.getParent().getParent();

        LinearLayout linearLayout = (LinearLayout)view1.getParent();
        playerDialog = linearLayout.findViewById(R.id.player_Dialog);
         firstRow    =  linearLayout.findViewById(R.id.firstRowLayout);

        TextView filePath = (TextView) firstRow.findViewById(R.id.file_path);
        String filePathMediaFile = filePath.getText().toString();
        System.out.println("Ramesh filePath :" + filePathMediaFile);


            mediaPlayerActivity = new MediaPlayerActivity(context,playerDialog);


       // if(draggedPosition>0)
         //   mediaPlayerActivity.draggedPosition=draggedPosition;

        mediaPlayerActivity.playAudioFile(filePathMediaFile);

        firstRow.setBackgroundColor(context.getResources().getColor(R.color.holo_blue_bright));
         secondRow    =linearLayout.findViewById(R.id.secondRowLayout);

        secondRow.setVisibility(View.GONE);
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


        ViewHolderChildItem(View viewHolderChildItem) {
            seekBar = (SeekBar) viewHolderChildItem.findViewById(R.id.seekBar);
            filePath = (TextView) viewHolderChildItem.findViewById(R.id.file_path);
            callDate = (TextView) viewHolderChildItem.findViewById(R.id.call_date);
            callTime = (TextView) viewHolderChildItem.findViewById(R.id.call_time);
            TranscriptedText = (TextView) viewHolderChildItem.findViewById(R.id.transcribedText);
            functionPlay = (ImageView) viewHolderChildItem.findViewById(R.id.functionPlay);
            pauseButton = (ImageView) viewHolderChildItem.findViewById(R.id.playPauseBtn);
            stopButton = (ImageView) viewHolderChildItem.findViewById(R.id.playstopButton);




        }
    }

}



