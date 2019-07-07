package com.rameshpenta.callRecorder;

import android.app.*;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.*;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CursorTreeAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InBoxFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class InBoxFragment extends Fragment  {


    private OnFragmentInteractionListener mListener;
    AudioManager audioManager;
    ExpandableListView inboxExpandableListView;
    TextView inboxEmpty;
    CallRecordDataBase dataBase;
    ImageView drawerIcon;
    ImageView search;
    ImageView delete;
    ImageView sync ;
    ImageView clear;
    ImageView speaker;
    Toolbar toolbarBottom;
    EditText searchEditText;
    TextView folderName;
    Cursor callerTable;
    ImageView emptyInbox;
    LinearLayout inboxHeader;
    LinearLayout inboxHeader_search;
    Cursor callerRecordTable;
    Cursor callertable1;
    ExpandableListAdapter inboxExpandableListAdapter;
    ExpandableListAdapterSimpleCursor expandableListAdapterSimpleCursor;
    ExpandableListAdapter inboxExpandableListBydDateAdapter;
    ExpandableListAdapterSimpleCursorByDate expandableListAdapterSimpleCursorByDate;
    boolean sortByDate=false;
    boolean sortBysender=false;
    boolean sortByMonth=false;
    boolean audio_mute = false;
    int totalMessages=0;
    String groupOnClickPhoneNumber;
    String groupOnClickDate;
    String saveSearch="";
    boolean previousStateOnStop=false;







    StringBuffer searchString;


    public InBoxFragment() {
        // Required empty public constructor
    }

    


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            Bundle bundle=this.getArguments();
        final String menuItem = bundle.getString("MenuItem");
        // Inflate the layout for this fragment
        System.out.println("Ramesh Inbox Fragment");
        View view = inflater.inflate(R.layout.fragment_in_box, container, false);

        inboxHeader_search = (LinearLayout) view.findViewById(R.id.inbox_header_search);
        inboxHeader = (LinearLayout) view.findViewById(R.id.inbox_header);
        inboxExpandableListView = (ExpandableListView) view.findViewById(R.id.expandable_list_view);
        toolbarBottom= (Toolbar) view.findViewById(R.id.toolBarBottom);
        inboxEmpty = (TextView) view.findViewById(R.id.inboxEmpty);
        drawerIcon = (ImageView) view.findViewById(R.id.drawer_icon);

        search = (ImageView) view.findViewById(R.id.search_imageView_inbox);
        speaker = (ImageView) view.findViewById(R.id.speaker);
        delete = (ImageView) view.findViewById(R.id.delete_imageView_inbox);
        sync = (ImageView) view.findViewById(R.id.sync_imageview_inbox);
        clear =(ImageView) view.findViewById(R.id.clear);
        emptyInbox = (ImageView) view.findViewById(R.id.empty);
        folderName = (TextView) view.findViewById(R.id.currentFolder);
        folderName.setText("    " + "INBOX");
        mListener.updateActionBarTitle(totalMessages);

        searchEditText = (EditText) view.findViewById(R.id.searchEditText);
        toolbarBottom.inflateMenu(R.menu.menu_action_bar_bottom);
        toolbarBottom.getMenu().getItem(3).setVisible(false);
        setInboxMenuItemsInvisible();
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        dataBase = new CallRecordDataBase(getActivity());
        dataBase.openDatabase();

        String[] menuItems=getResources().getStringArray(R.array.titles);
        if (menuItem.equals(menuItems[1])) {
            System.out.println("Ramesh InboxFragment Inbox");

            callerTable = dataBase.getCallerInfo();
            if (callerTable.getCount() > 0) {

                setInboxExpandableListView();

            }

        }


        if (menuItem.equals(menuItems[3])) {
            System.out.println("Ramesh InboxFragment sort By Sender");
            sortBysender=true;
            callerTable = dataBase.getCallerInfoSortByName();
            if (callerTable.getCount() > 0)
            {
                setInboxExpandableListView();

            }


        }

        if (menuItem.equals(menuItems[4]))
        {
            sortByDate=true;
            System.out.println("Ramesh InboxFragment sort By Date");
            callerRecordTable =dataBase.getCallerRecordsUniqueDate();
            if(callerRecordTable.getCount() > 0)
                setInboxExpandableListViewByDate();
    }
        if (menuItem.equals(menuItems[5]))
        {
            sortByMonth=true;
            System.out.println("Ramesh InboxFragment sort By Month");
            callerRecordTable =dataBase.getCallerRecordsUniqueMonth();
            if(callerRecordTable.getCount() > 0)
                setInboxExpandableListViewByDate();
        }

        drawerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnDrawerIconClicked();


            }
        });



        drawerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnDrawerIconClicked();


            }
        });

        inboxExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {


                if (sortByDate || sortByMonth) {
                    if (expandableListAdapterSimpleCursorByDate != null)
                    {
                        String callDateMMM;
                        String callDateMMMDDYYYY;
                        String callDateYYYY;


                        System.out.println("Ramesh grouo click listner");
                        TextView groupDateOnClick= (TextView) v.findViewById(R.id.call_date_group_item);
    //                    CheckBox groupDateCheckBox = (CheckBox) v.findViewById(R.id.call_date_group_item_checkbox);

                        if(sortByDate)
                        {
                            String callDateInFormat = groupDateOnClick.getText().toString().trim();
                            callDateMMM = Utils.getMonthName(Integer.parseInt(callDateInFormat.substring(4, 6)));
                            callDateMMMDDYYYY = callDateMMM + callDateInFormat.substring(6, 8) + "\n" + callDateInFormat.substring(0, 4);

                            groupOnClickDate = callDateMMMDDYYYY;
                        }
                        else
                        {
                            String callDateInFormat = groupDateOnClick.getText().toString().trim();
                             callDateMMM = Utils.getMonthName(Integer.parseInt(callDateInFormat.substring(4, 6)));
                             callDateYYYY=callDateInFormat.substring(0, 4);
                        }

//                        if(groupDateCheckBox.isChecked())
  //                          groupDateCheckedForDelete.add(callDateInFormat);


                        if(expandableListAdapterSimpleCursorByDate.dateOfCall != null)
                        {

                            if(sortByDate) {
                                if (expandableListAdapterSimpleCursorByDate.dateOfCall.equals(groupOnClickDate))
                                    stopMediaPlayerIfPlaying();
                            }
                            else
                            {
                                if(expandableListAdapterSimpleCursorByDate.dateOfCall.substring(0,3).equals(callDateMMM))
                                    stopMediaPlayerIfPlaying();

                            }
                        }


                    }


                } else {

                    TextView groupOnClick= (TextView) v.findViewById(R.id.contact_number);
                    groupOnClickPhoneNumber =groupOnClick.getText().toString().trim();
                    if (expandableListAdapterSimpleCursor != null) {



                        if (expandableListAdapterSimpleCursor.phoneNumber != null)
                        {

                            if (expandableListAdapterSimpleCursor.phoneNumber.equals(groupOnClickPhoneNumber))
                                stopMediaPlayerIfPlaying();
                    }

                    }
                }



                return false;
            }
        });

        toolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.speaker_icon || id == R.id.speaker) {

                    System.out.println("Ramesh Speaker onClick Listner");
                    audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

                    if (!audio_mute) {

                        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                        toolbarBottom.getMenu().getItem(0).setIcon(R.drawable.mute_icon);
                        toolbarBottom.getMenu().getItem(5).setTitle("Speaker");
                        audio_mute = true;
                    } else {
                        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);

                        toolbarBottom.getMenu().getItem(0).setIcon(R.drawable.speaker_icon);
                        toolbarBottom.getMenu().getItem(5).setTitle("Mute");
                        audio_mute = false;

                    }

                }
                if (id == R.id.delete_icon ||id == R.id.delete )
                    delete();

                if (id == R.id.sync_icon || id == R.id.sync )
                    sync();

                if (id == R.id.emptyInbox_icon || id == R.id.emptyInbox)
                    emptyInbox();




                return false;
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (sortByMonth ) {
                    System.out.println("Ramesh search String sorty by date " + s.toString());
                    callerRecordTable = dataBase.getCallerRecordsUniqueMonthBySearch(s.toString());
                    if (callerRecordTable.getCount() > 0) {
                        System.out.println("Ramesh search string " + s.toString());
                        setInboxExpandableListViewByDate();
                    }

                }
                else
                if (sortByDate ) {
                    System.out.println("Ramesh search String sorty by date " + s.toString());
                    callerRecordTable = dataBase.getCallerRecordsUniqueDateBySearch(s.toString());
                    if (callerRecordTable.getCount() > 0) {
                        System.out.println("Ramesh search string " + s.toString());
                        setInboxExpandableListViewByDate();
                    }

                } else

                {

                    callerTable = dataBase.callerTableSearch(s.toString());
                    if (callerTable.getCount() > 0) {

                        setInboxExpandableListView();
                    }
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                System.out.println("Ramesh after changed");
            }
        });


                inboxExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                        System.out.println("Ramesh on Child Click Listener");
                        return false;
                    }
                });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("Ramesh Search onClickListener");
                folderName.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                inboxHeader_search.setVisibility(View.VISIBLE);
                searchEditText.requestFocus();


            }
        });

        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Ramesh Speaker onClick Listner");
                audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

                if(!audio_mute) {

                    audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    speaker.setImageResource(R.drawable.mute_icon);
                    audio_mute=true;
                }
                else
                {
                    audioManager.setStreamMute(AudioManager.STREAM_MUSIC,false);
                    speaker.setImageResource(R.drawable.spear_icon);
                    audio_mute=false;

                }
                ;

            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Ramesh Clear onClick Listner ");
                    searchEditText.setText("");
            }
        });



        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Ramesh Sync ");
                if ( sortByMonth) {

                    callerRecordTable =dataBase.getCallerRecordsUniqueMonth();
                    if(callerRecordTable.getCount() > 0)
                        setInboxExpandableListViewByDate();

                }

                if (sortByDate ) {

                    callerRecordTable =dataBase.getCallerRecordsUniqueDate();
                    if(callerRecordTable.getCount() > 0)
                        setInboxExpandableListViewByDate();

                } else
                if(sortBysender)
                {
                    callerTable = dataBase.getCallerInfoSortByName();
                    if (callerTable.getCount() > 0)
                    {
                        setInboxExpandableListView();

                    }

                }
                else
                {

                    callerTable = dataBase.getCallerInfo();
                    if (callerTable.getCount() > 0)
                    {

                        setInboxExpandableListView();

                    }


                }






            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    System.out.println("Ramesh delete ");
                boolean atLeastOneRecordChecked = false;
                if (expandableListAdapterSimpleCursor != null) {
                    if (expandableListAdapterSimpleCursor.checkedForDelete.size() > 0)
                        atLeastOneRecordChecked = true;

                }
                if (expandableListAdapterSimpleCursorByDate != null)
                {
                    if (expandableListAdapterSimpleCursorByDate.checkedForDelete.size() > 0)
                        atLeastOneRecordChecked = true;

                }

                if (atLeastOneRecordChecked)
                {

                    Bundle bundle = new Bundle();
                bundle.putBoolean("DeleteInbox", true);

                android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
                commonDialogFragment.setArguments(bundle);
                commonDialogFragment.show(getActivity().getFragmentManager(), "CommonDialog");

            }


            }
        });

        emptyInbox.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {


                    Bundle bundle = new Bundle();
                    bundle.putBoolean("EmptyInbox", true);
                    android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
                    commonDialogFragment.setArguments(bundle);
                    commonDialogFragment.show(getActivity().getFragmentManager(), "CommonDialog");




            }
        });


        return view;
    }

    private void setInboxExpandableListView()
    {

        System.out.println("Ramesh setInboxExpandableListView");

         /*expandableListAdapterSimpleCursor= new ExpandableListAdapterSimpleCursor(getActivity(), callerTable, R.layout.group_view_list_item, new String[]{"CALLER_NAME", "PHONE_NUMBER", "NUMBER_OF_CALLS","CONTACT_IMAGE_URI"},
                new int[]{R.id.contact_name, R.id.contact_number, R.id.no_of_calls,R.id.contact_image},
                R.layout.inbox_list_item_expanded, new String[]{"FILE_PATH","CALL_DATE", "CALL_TIME"},
                new int[]{R.id.file_path, R.id.call_date, R.id.call_time},dataBase);*/


        expandableListAdapterSimpleCursor=new ExpandableListAdapterSimpleCursor(callerTable,getActivity(),dataBase);


        totalMessages=dataBase.getAllCallRecords().getCount();
        folderName.setText("    " + "INBOX(" + totalMessages + ")");
        mListener.updateActionBarTitle(totalMessages);
        inboxExpandableListAdapter = expandableListAdapterSimpleCursor;

        inboxExpandableListView.setAdapter(inboxExpandableListAdapter);


        inboxEmpty.setVisibility(View.GONE);


    }

    private void setInboxExpandableListViewByDate()
    {
        System.out.println("Ramesh setInboxExpandableListViewByDate");


             /* expandableListAdapterSimpleCursorByDate= new ExpandableListAdapterSimpleCursorByDate(getActivity(), callerRecordTable, R.layout.group_view_list_item_bydate, new String[]{"CALL_DATE"},
                        new int[]{R.id.call_date_group_item},
                        R.layout.inbox_list_item_expanded, new String[]{"FILE_PATH","PHONE_NUMBER","CALL_DATE", "CALL_TIME"},
                        new int[]{R.id.file_path,R.id.phone_number, R.id.call_date, R.id.call_time},dataBase);*/

        expandableListAdapterSimpleCursorByDate=new ExpandableListAdapterSimpleCursorByDate(callerRecordTable,getActivity(),dataBase,sortByDate);

        inboxExpandableListBydDateAdapter=expandableListAdapterSimpleCursorByDate;

        inboxExpandableListView.setAdapter(inboxExpandableListBydDateAdapter);
        totalMessages=dataBase.getAllCallRecords().getCount();
        folderName.setText("    " + "INBOX(" + totalMessages + ")");
        mListener.updateActionBarTitle(totalMessages);

        inboxEmpty.setVisibility(View.GONE);



    }



    @Override
    public void onResume() {


        if(expandableListAdapterSimpleCursor !=null) {
            View saveView = expandableListAdapterSimpleCursor.saveView;

            if (saveView != null) {
                System.out.println("Ramesh saveView not Null");
                expandableListAdapterSimpleCursor.displayMediaPlayer(saveView);
            }



        }

        if(expandableListAdapterSimpleCursorByDate !=null) {
            View saveView = expandableListAdapterSimpleCursorByDate.saveView;

            if (saveView != null) {
                System.out.println("Ramesh saveView not Null");
                expandableListAdapterSimpleCursorByDate.displayMediaPlayer(saveView);
            }


        }

        super.onResume();
    }

    @Override
    public void onPause() {
        System.out.println("Ramesh onPause");
        if(expandableListAdapterSimpleCursor !=null) {

            if (expandableListAdapterSimpleCursor.mediaPlayerActivity != null)
                expandableListAdapterSimpleCursor.mediaPlayerActivity.StopActiveMusicPlayer();
        }

        if(expandableListAdapterSimpleCursorByDate !=null) {

            if (expandableListAdapterSimpleCursorByDate.mediaPlayerActivity != null)
                expandableListAdapterSimpleCursorByDate.mediaPlayerActivity.StopActiveMusicPlayer();
        }

        super.onPause();
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        System.out.println("Ramesh Inbox Fragment onAttach");
        try {
            mListener = (OnFragmentInteractionListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        System.out.println("Ramesh IboxFragment on Detach");
        mListener = null;
    }
    public void setInboxMenuItemsInvisible()
    {

        toolbarBottom.getMenu().getItem(3).setVisible(false);
        toolbarBottom.getMenu().getItem(5).setTitle("Mute");
        toolbarBottom.getMenu().getItem(9).setVisible(false);
        toolbarBottom.getMenu().getItem(10).setVisible(false);

    }


    public void onConfirmEmptyInbox()
    {
        System.out.println("Ramesh  onConfirmEmptyInbox");
        stopMediaPlayerIfPlaying();
        dataBase.emptyInboxDatabase(sortByMonth);

        if (sortBysender)
        {


            if(callerTable!=null)
                callerTable.close();
            callerTable = dataBase.getCallerInfoSortByName();
            expandableListAdapterSimpleCursor.changeCursor(callerTable);
            expandableListAdapterSimpleCursor.notifyDataSetChanged();
            if (callerTable.getCount() == 0)
            {
                mListener.toInitialiseSearchQuery();
                inboxEmpty.setVisibility(View.VISIBLE);


            }



        }
        else
        if(sortByDate || sortByMonth)
        {

            if(callerRecordTable!=null)
                callerRecordTable.close();
            if(sortByDate)
                callerRecordTable =dataBase.getCallerRecordsUniqueDate();
            else
                callerRecordTable =dataBase.getCallerRecordsUniqueMonth();

            expandableListAdapterSimpleCursorByDate.changeCursor(callerRecordTable);
            expandableListAdapterSimpleCursorByDate.notifyDataSetChanged();

            if(callerRecordTable.getCount() == 0) {
                mListener.toInitialiseSearchQuery();
                inboxEmpty.setVisibility(View.VISIBLE);

            }



        }
        else
        {
            if(callerTable!=null)
                callerTable.close();

            callerTable = dataBase.getCallerInfo();
            expandableListAdapterSimpleCursor.changeCursor(callerTable);
            expandableListAdapterSimpleCursor.notifyDataSetChanged();

            if (callerTable.getCount() == 0)
            {
                mListener.toInitialiseSearchQuery();
                inboxEmpty.setVisibility(View.VISIBLE);


            }



        }


        if (expandableListAdapterSimpleCursor != null) {
            if (expandableListAdapterSimpleCursor.checkedForDelete.size() > 0)
                expandableListAdapterSimpleCursor.checkedForDelete.removeAll(expandableListAdapterSimpleCursor.checkedForDelete);

        }
        if (expandableListAdapterSimpleCursorByDate != null)
        {
            if (expandableListAdapterSimpleCursorByDate.checkedForDelete.size() > 0)
                expandableListAdapterSimpleCursorByDate.checkedForDelete.removeAll(expandableListAdapterSimpleCursorByDate.checkedForDelete);

        }



        if(!sortByMonth)
        totalMessages=dataBase.getAllCallRecords().getCount();
        else
            totalMessages=dataBase.getAllCallRecordsMonth().getCount();
        folderName.setText("    " + "INBOX(" + totalMessages + ")");
        mListener.updateActionBarTitle(totalMessages);
        if(!sortByMonth)
        Toast.makeText(getActivity(), "Inbox Empty!! All Records in Inbox has been moved to Trash ", Toast.LENGTH_SHORT).show();



    }

    public void onConfirmDelete() {
        String filePath;
        String callDate;
        String phoneNumber;
        int noOfRecordsForDelete;
        int noOfRecordsForGroupDelete;
            System.out.println("Ramesh onConfirmDelete");

        stopMediaPlayerIfPlaying();
        if (sortBysender)
        {

             noOfRecordsForDelete = expandableListAdapterSimpleCursor.checkedForDelete.size();


            for (int i = 0; i < noOfRecordsForDelete; i++) {
                filePath = expandableListAdapterSimpleCursor.checkedForDelete.get(i);
                dataBase.moveRecordsToTrashDatabase(filePath,sortByMonth);

            }
            noOfRecordsForGroupDelete=expandableListAdapterSimpleCursor.checkedForGroupDelete.size();

            for (int i = 0; i < noOfRecordsForGroupDelete; i++) {
                phoneNumber = expandableListAdapterSimpleCursor.checkedForGroupDelete.get(i);
                dataBase.moveRecordsToTrashDatabaseByPhoneNumber(phoneNumber);

            }

            expandableListAdapterSimpleCursor.checkedForDelete.removeAll(expandableListAdapterSimpleCursor.checkedForDelete);
            expandableListAdapterSimpleCursor.checkedForGroupDelete.removeAll(expandableListAdapterSimpleCursor.checkedForGroupDelete);

            if(callerTable!=null)
                callerTable.close();
            callerTable = dataBase.callerTableSearchSortByName(saveSearch);
            expandableListAdapterSimpleCursor.changeCursor(callerTable);
            expandableListAdapterSimpleCursor.notifyDataSetChanged();
            if (callerTable.getCount() == 0)
            {
                callerTable = dataBase.getCallerInfoSortByName();
                if(callerTable.getCount() == 0) {
                    mListener.toInitialiseSearchQuery();
                    inboxEmpty.setVisibility(View.VISIBLE);

                }

            }



        }
        else
        if(sortByDate || sortByMonth)
        {
             noOfRecordsForDelete = expandableListAdapterSimpleCursorByDate.checkedForDelete.size();
            for (int i = 0; i < noOfRecordsForDelete; i++) {
                filePath = expandableListAdapterSimpleCursorByDate.checkedForDelete.get(i);
                dataBase.moveRecordsToTrashDatabase(filePath,sortByMonth);

            }

             noOfRecordsForGroupDelete = expandableListAdapterSimpleCursorByDate.checkedForGroupDelete.size();
            for (int i = 0; i < noOfRecordsForGroupDelete; i++) {
                callDate = expandableListAdapterSimpleCursorByDate.checkedForGroupDelete.get(i);
                dataBase.moveRecordsToTrashDatabaseByCallDate(callDate,sortByDate);

            }

            expandableListAdapterSimpleCursorByDate.checkedForDelete.removeAll(expandableListAdapterSimpleCursorByDate.checkedForDelete);
            expandableListAdapterSimpleCursorByDate.checkedForGroupDelete.removeAll(expandableListAdapterSimpleCursorByDate.checkedForGroupDelete);

            if(callerRecordTable!=null)
                callerRecordTable.close();
            if(sortByDate)
            callerRecordTable =dataBase.getCallerRecordsUniqueDateBySearch(saveSearch);
            else
                callerRecordTable =dataBase.getCallerRecordsUniqueMonthBySearch(saveSearch);

            expandableListAdapterSimpleCursorByDate.changeCursor(callerRecordTable);
            expandableListAdapterSimpleCursorByDate.notifyDataSetChanged();

            if(callerRecordTable.getCount() == 0) {
                if(sortByDate)
                callerRecordTable =dataBase.getCallerRecordsUniqueDate();
                else
                    callerRecordTable =dataBase.getCallerRecordsUniqueMonth();
                if(callerRecordTable.getCount() == 0) {
                    mListener.toInitialiseSearchQuery();
                    inboxEmpty.setVisibility(View.VISIBLE);

                }
            }



        }
        else
        {
             noOfRecordsForDelete = expandableListAdapterSimpleCursor.checkedForDelete.size();
            for (int i = 0; i < noOfRecordsForDelete; i++) {
                filePath = expandableListAdapterSimpleCursor.checkedForDelete.get(i);
                dataBase.moveRecordsToTrashDatabase(filePath,sortByMonth);

            }
            noOfRecordsForGroupDelete=expandableListAdapterSimpleCursor.checkedForGroupDelete.size();

            for (int i = 0; i < noOfRecordsForGroupDelete; i++) {
                phoneNumber = expandableListAdapterSimpleCursor.checkedForGroupDelete.get(i);
                dataBase.moveRecordsToTrashDatabaseByPhoneNumber(phoneNumber);

            }

            expandableListAdapterSimpleCursor.checkedForDelete.removeAll(expandableListAdapterSimpleCursor.checkedForDelete);
            expandableListAdapterSimpleCursor.checkedForGroupDelete.removeAll(expandableListAdapterSimpleCursor.checkedForGroupDelete);



            if(callerTable!=null)
                callerTable.close();

            callerTable = dataBase.callerTableSearch(saveSearch);
            setInboxExpandableListView();
  //          expandableListAdapterSimpleCursor.changeCursor(callerTable);
//            expandableListAdapterSimpleCursor.notifyDataSetChanged();

            if (callerTable.getCount() == 0)
            {
                callerTable = dataBase.getCallerInfo();
                if(callerTable.getCount() == 0)
                {
                    mListener.toInitialiseSearchQuery();

                    inboxEmpty.setVisibility(View.VISIBLE);

                }

            }



        }

        if(!sortByMonth)
        totalMessages=dataBase.getAllCallRecords().getCount();
        else
            totalMessages=dataBase.getAllCallRecordsMonth().getCount();
        folderName.setText("    " + "INBOX(" + totalMessages + ")");
        mListener.updateActionBarTitle(totalMessages);
        if(!sortByMonth)
        Toast.makeText(getActivity(), "Selected Records has been moved to Trash ", Toast.LENGTH_SHORT).show();



    }


    public void showMatchingRecords(String search)
    {
        saveSearch=search;

        stopMediaPlayerIfPlaying();

        if (sortByDate || sortByMonth) {
            System.out.println("Ramesh search String sorty by date " + search);
            if(sortByDate)
            callerRecordTable = dataBase.getCallerRecordsUniqueDateBySearch(search);
            else
                callerRecordTable = dataBase.getCallerRecordsUniqueMonthBySearch(search);
            //if (callerRecordTable.getCount() > 0) {
                System.out.println("Ramesh search string " + search);
                setInboxExpandableListViewByDate();
            //}

        } else

        if(sortBysender)
        {
            callerTable = dataBase.callerTableSearchSortByName(search);
          //  if (callerTable.getCount() > 0) {

                setInboxExpandableListView();
           // }
        }
        else
        {

            callerTable = dataBase.callerTableSearch(search);
            //if (callerTable.getCount() > 0) {

                setInboxExpandableListView();
            //}



        }




    }

        void sync()
        {

            System.out.println("Ramesh Sync ");
            stopMediaPlayerIfPlaying();
            if (sortByDate || sortByMonth) {
                if(sortByDate)
                callerRecordTable =dataBase.getCallerRecordsUniqueDateBySearch(saveSearch);
                else
                    callerRecordTable =dataBase.getCallerRecordsUniqueMonthBySearch(saveSearch);
                if(callerRecordTable.getCount() > 0)
                    setInboxExpandableListViewByDate();

            } else
            if(sortBysender)
            {
                callerTable = dataBase.callerTableSearchSortByName(saveSearch);
                if (callerTable.getCount() > 0)
                {
                    setInboxExpandableListView();

                }

            }
            else
            {

                callerTable = dataBase.callerTableSearch(saveSearch);
                if (callerTable.getCount() > 0)
                {

                    setInboxExpandableListView();

                }


            }



            Toast.makeText(getActivity(), "Screen is Refreshed for latest call records ", Toast.LENGTH_SHORT).show();


        }

     void delete()
     {


         System.out.println("Ramesh delete ");
         boolean atLeastOneRecordChecked = false;
         boolean atLeastOneGroupRecordChecked=false;
         if (expandableListAdapterSimpleCursor != null) {
             if (expandableListAdapterSimpleCursor.checkedForDelete.size() > 0)
                 atLeastOneRecordChecked = true;
             if(expandableListAdapterSimpleCursor.checkedForGroupDelete.size() > 0)
                 atLeastOneGroupRecordChecked=true;

         }
         if (expandableListAdapterSimpleCursorByDate != null)
         {
             if (expandableListAdapterSimpleCursorByDate.checkedForDelete.size() > 0)
                 atLeastOneRecordChecked = true;
             if(expandableListAdapterSimpleCursorByDate.checkedForGroupDelete.size() > 0)
                  atLeastOneGroupRecordChecked=true;

         }


         if (atLeastOneRecordChecked || atLeastOneGroupRecordChecked )
         {

             Bundle bundle = new Bundle();
             bundle.putBoolean("DeleteInbox", true);

             android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
             commonDialogFragment.setArguments(bundle);
             commonDialogFragment.show(getActivity().getFragmentManager(), "CommonDialog");
         }
         else
             Toast.makeText(getActivity(), "No Record is Checked for Delete ", Toast.LENGTH_SHORT).show();





     }

    void emptyInbox()
    {



        if(inboxEmpty.getVisibility() != View.VISIBLE) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("EmptyInbox", true);
            android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
            commonDialogFragment.setArguments(bundle);
            commonDialogFragment.show(getActivity().getFragmentManager(), "CommonDialog");

        }

        else
            Toast.makeText(getActivity(), "Inbox Empty ", Toast.LENGTH_SHORT).show();



    }

    void speaker()
    {

        System.out.println("Ramesh Speaker onClick Listner");
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        if(!audio_mute) {

            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            speaker.setImageResource(R.drawable.mute_icon);
            audio_mute=true;
        }
        else
        {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC,false);
            speaker.setImageResource(R.drawable.spear_icon);
            audio_mute=false;

        }
        ;


    }
    void stopMediaPlayerIfPlaying()
    {
        System.out.println("Ramesh stop Media Player if Playing");
        if (sortByDate || sortByMonth) {
            if (expandableListAdapterSimpleCursorByDate != null)
            {

                if (audioManager.isMusicActive() && expandableListAdapterSimpleCursorByDate.mediaPlayerActivity != null) {


                    expandableListAdapterSimpleCursorByDate.mediaPlayerActivity.StopActiveMusicPlayer();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 100);
                    System.out.println("Ramesh stop active Music Player");

                }

        }


        } else {
            if (expandableListAdapterSimpleCursor != null)
            {
                if (audioManager.isMusicActive() && expandableListAdapterSimpleCursor.mediaPlayerActivity != null)
                {


                    expandableListAdapterSimpleCursor.mediaPlayerActivity.StopActiveMusicPlayer();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 100);
                    System.out.println("Ramesh stop active Music Player");

                }
           }
        }




    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
        public void OnDrawerIconClicked();
        public void toInitialiseSearchQuery();
        public void updateActionBarTitle(int i);
    }

    @Override
    public void onStop() {
        previousStateOnStop=true;
        super.onStop();
        System.out.println("Ramesh on Stop");
    }
}

