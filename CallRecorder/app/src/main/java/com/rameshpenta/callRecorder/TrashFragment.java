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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.*;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InBoxFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class TrashFragment extends Fragment  {

    private OnFragmentInteractionListener mListener;
    AudioManager audioManager;
    ExpandableListView trashExpandableListView;
    TextView trashEmpty;
    CallRecordTrashDatabase dataBase;
    ImageView drawerIcon;
    Toolbar toolbarBottom;
    ImageView search;
    ImageView delete;
    ImageView moveToInbox ;
    ImageView clear;
    ImageView speaker;

    EditText searchEditText;
    TextView folderName;
    Cursor callerTable;
    ImageView emptyTrash;
    LinearLayout trashHeader;
    LinearLayout trashHeader_search;
    Cursor callerRecordTable;
    Cursor callertable1;
    ExpandableListAdapter trashExpandableListAdapter;
    ExpandableListAdapterSimpleCursor expandableListAdapterSimpleCursor;
    ExpandableListAdapter trashExpandableListBydDateAdapter;
    ExpandableListAdapterSimpleCursorByDate expandableListAdapterSimpleCursorByDate;
    boolean sortByDate=false;
    boolean sortByMonth=false;
    boolean sortBysender=false;
    boolean audio_mute = false;
    int totalMessages=0;
    String groupOnClickPhoneNumber;
    String groupOnClickDate;
    String saveSearch="";



    StringBuffer searchString;

    public TrashFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle=this.getArguments();
        String menuItem = bundle.getString("MenuItem");
        // Inflate the layout for this fragment
        System.out.println("Ramesh Trash Fragment");
        View view = inflater.inflate(R.layout.fragment_in_box, container, false);
        trashHeader_search = (LinearLayout) view.findViewById(R.id.inbox_header_search);
        trashHeader = (LinearLayout) view.findViewById(R.id.inbox_header);
        trashExpandableListView = (ExpandableListView) view.findViewById(R.id.expandable_list_view);
        toolbarBottom= (Toolbar) view.findViewById(R.id.toolBarBottom);
        trashEmpty = (TextView) view.findViewById(R.id.inboxEmpty);
        drawerIcon = (ImageView) view.findViewById(R.id.drawer_icon);
        search = (ImageView) view.findViewById(R.id.search_imageView_inbox);
        speaker = (ImageView) view.findViewById(R.id.speaker);
        delete = (ImageView) view.findViewById(R.id.delete_imageView_inbox);
        moveToInbox = (ImageView) view.findViewById(R.id.sync_imageview_inbox);
        moveToInbox.setImageResource(R.drawable.ic_move_to_inbox);

        clear =(ImageView) view.findViewById(R.id.clear);
        emptyTrash = (ImageView) view.findViewById(R.id.empty);
        folderName = (TextView) view.findViewById(R.id.currentFolder);
        folderName.setText("    " + "TRASH(0)");
        mListener.updateActionBarTitle(totalMessages);
        toolbarBottom.inflateMenu(R.menu.menu_action_bar_bottom);
        toolbarBottom.getMenu().getItem(2).setVisible(false);
        setInboxMenuItemsInvisible();
        searchEditText = (EditText) view.findViewById(R.id.searchEditText);
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        dataBase = new CallRecordTrashDatabase(getActivity());
        dataBase.openDatabase();
        String[] menuItems=getResources().getStringArray(R.array.titles);
        if (menuItem.equals(menuItems[2])) {
            System.out.println("Ramesh TrashFragment Trash");

            callerTable = dataBase.getCallerInfo();
            if (callerTable.getCount() > 0) {

                setTrashExpandableListView();

            }


        }

        if (menuItem.equals(menuItems[3])) {
            System.out.println("Ramesh TrashFragment sort By Sender");
            sortBysender=true;
            callerTable = dataBase.getCallerInfoSortByName();
            if (callerTable.getCount() > 0)
            {
                setTrashExpandableListView();

            }


        }

        if (menuItem.equals(menuItems[4]))
        {
            sortByDate=true;
            System.out.println("Ramesh TrashFragment sort By Date");
            callerRecordTable =dataBase.getCallerRecordsUniqueDate();
            if(callerRecordTable.getCount() > 0)
                setTrashExpandableListViewByDate();


        }
        if (menuItem.equals(menuItems[5]))
        {
            sortByMonth=true;
            System.out.println("Ramesh TrashFragment sort By Date");
            callerRecordTable =dataBase.getCallerRecordsUniqueMonth();
            if(callerRecordTable.getCount() > 0)
                setTrashExpandableListViewByDate();


        }


        trashExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (sortByDate || sortByMonth ) {
                    if (expandableListAdapterSimpleCursorByDate != null)
                    {

                        if(sortByDate) {
                            TextView groupDateOnClick = (TextView) v.findViewById(R.id.call_date_group_item);
                            String callDateInFormat = groupDateOnClick.getText().toString().trim();
                            String callDateMMM = Utils.getMonthName(Integer.parseInt(callDateInFormat.substring(4, 6)));
                            String callDateMMMDDYYYY = callDateMMM + callDateInFormat.substring(6, 8) + "\n" + callDateInFormat.substring(0, 4);

                            groupOnClickDate = callDateMMMDDYYYY;
                        }
                        //groupOnClickDate =groupDateOnClick.getText().toString().trim();


                        if(expandableListAdapterSimpleCursorByDate.dateOfCall != null)
                        {
                            if(sortByDate) {
                                if (expandableListAdapterSimpleCursorByDate.dateOfCall.equals(groupOnClickDate))
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
                if (id == R.id.delete_icon || id == R.id.delete)
                    delete();

                if (id == R.id.move_to_inbox_icon || id == R.id.move_to_inbox)
                {    boolean atLeastOneRecordChecked = false;
                     boolean atLeastOneGroupRecordChecked= false;
                    if (expandableListAdapterSimpleCursor != null) {
                        if (expandableListAdapterSimpleCursor.checkedForDelete.size() > 0)
                            atLeastOneRecordChecked = true;
                        if (expandableListAdapterSimpleCursor.checkedForGroupDelete.size() > 0)
                            atLeastOneGroupRecordChecked = true;


                    }
                    if (expandableListAdapterSimpleCursorByDate != null)
                    {
                        if (expandableListAdapterSimpleCursorByDate.checkedForDelete.size() > 0)
                            atLeastOneRecordChecked = true;
                        if(expandableListAdapterSimpleCursorByDate.checkedForGroupDelete.size() > 0)
                            atLeastOneGroupRecordChecked=true;

                    }

                    if(atLeastOneRecordChecked || atLeastOneGroupRecordChecked)
                        moveToInbox();
                    else
                        Toast.makeText(getActivity(), "No Record is Checked for Move To Inbox", Toast.LENGTH_SHORT).show();


                }
                if (id == R.id.emptyInbox_icon || id == R.id.emptyTrash)
                    emptyTrash();

                return false;
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (sortByDate || sortByMonth) {
                    if(sortByDate)
                    callerRecordTable = dataBase.getCallerRecordsUniqueDateBySearch(s.toString());
                    else
                        callerRecordTable = dataBase.getCallerRecordsUniqueMonthBySearch(s.toString());
                    if (callerRecordTable.getCount() > 0) {

                        setTrashExpandableListViewByDate();
                    }

                } else

                {

                    callerTable = dataBase.callerTableSearch(s.toString());
                    if (callerTable.getCount() > 0) {

                        setTrashExpandableListView();
                    }
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                ;
            }
        });

        searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    trashHeader_search.setVisibility(View.GONE);
                    searchEditText.setVisibility(View.GONE);
                    searchEditText.setText("");
                    trashHeader.setVisibility(View.VISIBLE);

                    return true;
                } else {
                    return false;
                }
            }
        });

        trashExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                System.out.println("Ramesh on Child Click Listener");
                return false;
            }
        });

        drawerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnDrawerIconClicked();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    System.out.println("Ramesh Trash Search onCLickListener");
                folderName.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                trashHeader_search.setVisibility(View.VISIBLE);
                searchEditText.requestFocus();


            }
        });

        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Ramesh Speaker onClick Listener");
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

                searchEditText.setText("");
            }
        });



        moveToInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                if(atLeastOneRecordChecked)
                moveToInbox();


            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                    bundle.putBoolean("DeleteTrash", true);

                    android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
                    commonDialogFragment.setArguments(bundle);
                    commonDialogFragment.show(getActivity().getFragmentManager(), "CommonDialog");
                }


            }
        });

        emptyTrash.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                if(trashEmpty.getVisibility() != View.VISIBLE) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("EmptyTrash", true);
                    android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
                    commonDialogFragment.setArguments(bundle);
                    commonDialogFragment.show(getActivity().getFragmentManager(), "CommonDialog");

                }


            }
        });

        return view;
    }

    private void setTrashExpandableListView()
    {

        /*expandableListAdapterSimpleCursor= new ExpandableListAdapterSimpleCursor(getActivity(), callerTable, R.layout.group_view_list_item, new String[]{"CALLER_NAME", "PHONE_NUMBER", "NUMBER_OF_CALLS","CONTACT_IMAGE_URI"},
                new int[]{R.id.contact_name, R.id.contact_number, R.id.no_of_calls,R.id.contact_image},
                R.layout.inbox_list_item_expanded, new String[]{"FILE_PATH","CALL_DATE", "CALL_TIME"},
                new int[]{R.id.file_path, R.id.call_date, R.id.call_time},dataBase);*/
        expandableListAdapterSimpleCursor=new ExpandableListAdapterSimpleCursor(callerTable,getActivity(),dataBase);
        totalMessages=dataBase.getAllCallRecords().getCount();
        folderName.setText("    " + "TRASH(" + totalMessages + ")");
        mListener.updateActionBarTitle(totalMessages);
        trashExpandableListAdapter = expandableListAdapterSimpleCursor;

        trashExpandableListView.setAdapter(trashExpandableListAdapter);


        trashEmpty.setVisibility(View.GONE);


    }

    private void setTrashExpandableListViewByDate()
    {


       /* expandableListAdapterSimpleCursorByDate= new ExpandableListAdapterSimpleCursorByDate(getActivity(), callerRecordTable, R.layout.group_view_list_item_bydate, new String[]{"CALL_DATE"},
                new int[]{R.id.call_date_group_item},
                R.layout.inbox_list_item_expanded, new String[]{"FILE_PATH","PHONE_NUMBER","CALL_DATE", "CALL_TIME"},
                new int[]{R.id.file_path,R.id.phone_number, R.id.call_date, R.id.call_time},dataBase);*/

        expandableListAdapterSimpleCursorByDate=new ExpandableListAdapterSimpleCursorByDate(callerRecordTable,getActivity(),dataBase,sortByDate);

        trashExpandableListBydDateAdapter=expandableListAdapterSimpleCursorByDate;

        trashExpandableListView.setAdapter(trashExpandableListBydDateAdapter);
        totalMessages=dataBase.getAllCallRecords().getCount();
        folderName.setText("    " + "TRASH(" + totalMessages + ")");
        mListener.updateActionBarTitle(totalMessages);

        trashEmpty.setVisibility(View.GONE);



    }

    @Override
    public void onResume() {


        System.out.println("Ramesh onResume");
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
        System.out.println("Ramesh Trash Fragment onAttach");
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

        toolbarBottom.getMenu().getItem(2).setVisible(false);
        toolbarBottom.getMenu().getItem(5).setTitle("Mute");
        toolbarBottom.getMenu().getItem(6).setVisible(false);
        toolbarBottom.getMenu().getItem(8).setVisible(false);

    }

    public void onConfirmEmptyTrash()
    {
        stopMediaPlayerIfPlaying();
        dataBase.emptyTrashDatabase();
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
                trashEmpty.setVisibility(View.VISIBLE);

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
                trashEmpty.setVisibility(View.VISIBLE);
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
                trashEmpty.setVisibility(View.VISIBLE);

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



            totalMessages = dataBase.getAllCallRecords().getCount();
            folderName.setText("    " + "TRASH(" + totalMessages + ")");
            mListener.updateActionBarTitle(totalMessages);

        Toast.makeText(getActivity(), "All Records in Trash has been deleted. TRASH Empty", Toast.LENGTH_SHORT).show();

    }

    public void moveToInbox()
    {
        String filePath;
        String callDate;
        int noOfRecordsForGroupDelete;
        int noOfRecordsForDelete;
        String phoneNumber;
        System.out.println("Ramesh moveToInbox");
        stopMediaPlayerIfPlaying();
        if (sortBysender)
        {

             noOfRecordsForDelete = expandableListAdapterSimpleCursor.checkedForDelete.size();


            for (int i = 0; i < noOfRecordsForDelete; i++) {
                filePath = expandableListAdapterSimpleCursor.checkedForDelete.get(i);
                dataBase.moveRecordsToInbox(filePath);

            }
            noOfRecordsForGroupDelete=expandableListAdapterSimpleCursor.checkedForGroupDelete.size();
            for (int i = 0; i < noOfRecordsForGroupDelete; i++) {
                phoneNumber= expandableListAdapterSimpleCursor.checkedForGroupDelete.get(i);
                dataBase.moveRecordsToInboxByPhoneNumber(phoneNumber);

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
                    trashEmpty.setVisibility(View.VISIBLE);

                }

            }



        }
        else
        if(sortByDate || sortByMonth)
        {
             noOfRecordsForDelete = expandableListAdapterSimpleCursorByDate.checkedForDelete.size();
            for (int i = 0; i < noOfRecordsForDelete; i++) {
                filePath = expandableListAdapterSimpleCursorByDate.checkedForDelete.get(i);
                dataBase.moveRecordsToInbox(filePath);

            }

             noOfRecordsForGroupDelete = expandableListAdapterSimpleCursorByDate.checkedForGroupDelete.size();
            System.out.println("Ramesh Checked for Group Delete" + noOfRecordsForGroupDelete);
            for (int i = 0; i < noOfRecordsForGroupDelete; i++) {
                System.out.println();
                callDate = expandableListAdapterSimpleCursorByDate.checkedForGroupDelete.get(i);
                dataBase.moveRecordsToInboxByCallDate(callDate,sortByDate);

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

            if(callerRecordTable.getCount() == 0)
            {
                if(sortByDate)
                callerRecordTable =dataBase.getCallerRecordsUniqueDate();
                else
                    callerRecordTable =dataBase.getCallerRecordsUniqueMonth();
                if(callerRecordTable.getCount() == 0)
                {
                    mListener.toInitialiseSearchQuery();
                    trashEmpty.setVisibility(View.VISIBLE);

                }
            }



        }
        else
        {
             noOfRecordsForDelete = expandableListAdapterSimpleCursor.checkedForDelete.size();
            for (int i = 0; i < noOfRecordsForDelete; i++) {
                filePath = expandableListAdapterSimpleCursor.checkedForDelete.get(i);
                dataBase.moveRecordsToInbox(filePath);

            }

            noOfRecordsForGroupDelete=expandableListAdapterSimpleCursor.checkedForGroupDelete.size();
            for (int i = 0; i < noOfRecordsForGroupDelete; i++) {
                phoneNumber= expandableListAdapterSimpleCursor.checkedForGroupDelete.get(i);
                dataBase.moveRecordsToInboxByPhoneNumber(phoneNumber);

            }

            expandableListAdapterSimpleCursor.checkedForDelete.removeAll(expandableListAdapterSimpleCursor.checkedForDelete);
            expandableListAdapterSimpleCursor.checkedForGroupDelete.removeAll(expandableListAdapterSimpleCursor.checkedForGroupDelete);


            if(callerTable!=null)
                callerTable.close();

            callerTable = dataBase.callerTableSearch(saveSearch);
            expandableListAdapterSimpleCursor.changeCursor(callerTable);
            expandableListAdapterSimpleCursor.notifyDataSetChanged();

            if (callerTable.getCount() == 0)
            {
                callerTable = dataBase.getCallerInfo();
                if(callerTable.getCount() == 0)
                {
                    mListener.toInitialiseSearchQuery();

                    trashEmpty.setVisibility(View.VISIBLE);

                }

            }



        }


            totalMessages = dataBase.getAllCallRecords().getCount();
            folderName.setText("    " + "TRASH(" + totalMessages + ")");
            mListener.updateActionBarTitle(totalMessages);
        Toast.makeText(getActivity(), "Selected Records moved to Inbox", Toast.LENGTH_SHORT).show();






    }

    public void onConfirmTrashDelete() {
        String filePath;
        String callDate;
        int noOfRecordsForDelete;
        int noOfRecordsForGroupDelete;
        String phoneNumber;
        stopMediaPlayerIfPlaying();
        if (sortBysender)
        {

             noOfRecordsForDelete = expandableListAdapterSimpleCursor.checkedForDelete.size();


            for (int i = 0; i < noOfRecordsForDelete; i++) {
                filePath = expandableListAdapterSimpleCursor.checkedForDelete.get(i);
                dataBase.deleteAudioFilesPermanently(filePath,sortByMonth);

            }


            noOfRecordsForGroupDelete=expandableListAdapterSimpleCursor.checkedForGroupDelete.size();
            for (int i = 0; i < noOfRecordsForGroupDelete; i++) {
                phoneNumber= expandableListAdapterSimpleCursor.checkedForGroupDelete.get(i);
                dataBase.deleteAudioFilesPermanentlyByPhoneNumber(phoneNumber);

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
                if(callerTable.getCount() == 0 ) {
                    mListener.toInitialiseSearchQuery();

                    trashEmpty.setVisibility(View.VISIBLE);
                }

            }



        }
        else
        if(sortByDate || sortByMonth)
        {
             noOfRecordsForDelete = expandableListAdapterSimpleCursorByDate.checkedForDelete.size();
            for (int i = 0; i < noOfRecordsForDelete; i++) {
                filePath = expandableListAdapterSimpleCursorByDate.checkedForDelete.get(i);
                dataBase.deleteAudioFilesPermanently(filePath,sortByMonth);

            }

             noOfRecordsForGroupDelete = expandableListAdapterSimpleCursorByDate.checkedForGroupDelete.size();
            for (int i = 0; i < noOfRecordsForGroupDelete; i++) {
                callDate = expandableListAdapterSimpleCursorByDate.checkedForGroupDelete.get(i);
                dataBase.deleteAudioFilesPermanentlyByCallDate(callDate,sortByDate);

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

                if(callerRecordTable.getCount() == 0)
                {
                    mListener.toInitialiseSearchQuery();
                    trashEmpty.setVisibility(View.VISIBLE);
                }
            }



        }
        else
        {
             noOfRecordsForDelete = expandableListAdapterSimpleCursor.checkedForDelete.size();
            for (int i = 0; i < noOfRecordsForDelete; i++) {
                filePath = expandableListAdapterSimpleCursor.checkedForDelete.get(i);
                dataBase.deleteAudioFilesPermanently(filePath,sortByMonth);

            }

            noOfRecordsForGroupDelete=expandableListAdapterSimpleCursor.checkedForGroupDelete.size();
            for (int i = 0; i < noOfRecordsForGroupDelete; i++) {
                phoneNumber= expandableListAdapterSimpleCursor.checkedForGroupDelete.get(i);
                dataBase.deleteAudioFilesPermanentlyByPhoneNumber(phoneNumber);

            }

            expandableListAdapterSimpleCursor.checkedForDelete.removeAll(expandableListAdapterSimpleCursor.checkedForDelete);
            expandableListAdapterSimpleCursor.checkedForGroupDelete.removeAll(expandableListAdapterSimpleCursor.checkedForGroupDelete);

            if(callerTable!=null)
                callerTable.close();

            callerTable = dataBase.callerTableSearch(saveSearch);
            expandableListAdapterSimpleCursor.changeCursor(callerTable);
            expandableListAdapterSimpleCursor.notifyDataSetChanged();

            if (callerTable.getCount() == 0)
            {
                callerTable = dataBase.getCallerInfo();
                if(callerTable.getCount() == 0)
                {
                    mListener.toInitialiseSearchQuery();
                    trashEmpty.setVisibility(View.VISIBLE);
                }

            }



        }



            totalMessages = dataBase.getAllCallRecords().getCount();
            folderName.setText("    " + "TRASH(" + totalMessages + ")");
            mListener.updateActionBarTitle(totalMessages);
        Toast.makeText(getActivity(), "Selected Records has been Deleted", Toast.LENGTH_SHORT).show();



    }

    public void showMatchingRecords(String search)
    {
        saveSearch=search;

        if (sortByDate || sortByMonth) {
            System.out.println("Ramesh search String sorty by date " + search);
            if(sortByDate)
            callerRecordTable = dataBase.getCallerRecordsUniqueDateBySearch(search);
            else
                callerRecordTable = dataBase.getCallerRecordsUniqueMonthBySearch(search);
           // if (callerRecordTable.getCount() > 0) {
                System.out.println("Ramesh search string " + search);
                setTrashExpandableListViewByDate();
            //}

        } else

        if(sortBysender)
        {
            callerTable = dataBase.callerTableSearchSortByName(search);
            //if (callerTable.getCount() > 0) {

                setTrashExpandableListView();
            //}
        }
        else
        {

            callerTable = dataBase.callerTableSearch(search);
            //if (callerTable.getCount() > 0) {

                setTrashExpandableListView();
            //}



        }



    }



    void delete()
    {

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
            if (expandableListAdapterSimpleCursorByDate.checkedForGroupDelete.size() > 0)
                atLeastOneGroupRecordChecked= true;


        }

        if (atLeastOneRecordChecked || atLeastOneGroupRecordChecked)
        {

            Bundle bundle = new Bundle();
            bundle.putBoolean("DeleteTrash", true);

            android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
            commonDialogFragment.setArguments(bundle);
            commonDialogFragment.show(getActivity().getFragmentManager(), "CommonDialog");
        }

        else
            Toast.makeText(getActivity(), "No Record is Checked for delete ", Toast.LENGTH_SHORT).show();






    }

    void emptyTrash()
    {

        if(trashEmpty.getVisibility() != View.VISIBLE) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("EmptyTrash", true);
            android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
            commonDialogFragment.setArguments(bundle);
            commonDialogFragment.show(getActivity().getFragmentManager(), "CommonDialog");
        }
        else
            Toast.makeText(getActivity(), "Trash is Empty ", Toast.LENGTH_SHORT).show();






    }

    void stopMediaPlayerIfPlaying()
    {
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

                if (audioManager.isMusicActive() && expandableListAdapterSimpleCursor.mediaPlayerActivity != null) {


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
        super.onStop();
        System.out.println("Ramesh on Stop");
    }
}

