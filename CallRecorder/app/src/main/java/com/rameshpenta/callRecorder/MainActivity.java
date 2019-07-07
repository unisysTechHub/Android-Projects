package com.rameshpenta.callRecorder;

import android.app.Application;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements InBoxFragment.OnFragmentInteractionListener,TrashFragment.OnFragmentInteractionListener,CommonDialogFragment.OnFragmentInteractionListener,CommonDialogFragment.DeleteOnClickListener{
    private String[] menuTitles;
    Cursor cursor;
    private TypedArray menuIcons;
    ArrayList<TitleBarMenuItem> titleBartMenuItems;
    TitleBarListItem titleBarListItem;
    ListView menuBar;
    DrawerLayout drawerLayout;
    Fragment fragment;
    int position;
    int prevPosition=0;
    int prevInboxPosition=0;
    boolean whenLaunchApp=true;
    boolean inboxSelected=true;
    boolean trashSelected=false;
    boolean settingSelected=false;
    private ActionBarDrawerToggle mDrawerToggle;
    SearchView searchView;
    String prevText="";
    boolean firstTimeFlag=true;
    boolean previousSateOnStop=false;
    int defaultView;
    Menu menu;
    boolean viewDefault=false;
    MenuItem previousMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("Ramesh onCreate");
        menuBar = (ListView) findViewById(R.id.menuBar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        menuTitles = getResources().getStringArray(R.array.titles);
        menuIcons = getResources().obtainTypedArray(R.array.title_bar_icons);

        titleBartMenuItems = new ArrayList<TitleBarMenuItem>();

        for (int i = 0; i < 7; i++) {
            titleBartMenuItems.add(new TitleBarMenuItem(menuTitles[i], menuIcons.getResourceId(i, -1)));
        }

        titleBarListItem = new TitleBarListItem(getApplicationContext(), 0, titleBartMenuItems);
        menuBar.setAdapter(titleBarListItem);
        SharedPreferences forPassword = getSharedPreferences("AppPassword",MODE_PRIVATE);
        whenLaunchApp=forPassword.getBoolean("WhenApplaunched",true);

        if (whenLaunchApp)
        {

            passwordSetWhenAppLaunched();
            getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, new ContactChangeObserver(new Handler(), this));
            SharedPreferences.Editor forPasswordEditor = forPassword.edit();
            forPasswordEditor.putBoolean("WhenApplaunched", false);
            forPasswordEditor.commit();
            String defaultViewString=PreferenceManager.getDefaultSharedPreferences(this).getString("ViewList","3");
            defaultView=Integer.parseInt(defaultViewString);
            viewDefault=true;
            displayView(1);
            viewDefault=false;

            whenLaunchApp=false;
        }
        else {
            Bundle bundle = new Bundle();
            bundle.putBoolean("Password", true);
            android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
            commonDialogFragment.setCancelable(false);
            commonDialogFragment.setArguments(bundle);
            commonDialogFragment.show(this.getFragmentManager(), "CommonDialog");
            getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, new ContactChangeObserver(new Handler(), this));
            String defaultViewString=PreferenceManager.getDefaultSharedPreferences(this).getString("ViewList","3");
            defaultView=Integer.parseInt(defaultViewString);
            viewDefault=true;

            displayView(1);
            viewDefault=false;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);


        mDrawerToggle= new ActionBarDrawerToggle(this,drawerLayout,R.drawable.ic_launcher,R.string.app_name,R.string.app_name)
        {
            // @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void onDrawerClosed(View view) {
                //getSupportActionBar().setTitle("Call Recorder");
                // calling onPrepareOptionsMenu() to show action bar icons
                supportInvalidateOptionsMenu();
            }

            // @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void onDrawerOpened(View drawerView) {
                //getSupportActionBar().setTitle("Call Recorder");
                // calling onPrepareOptionsMenu() to hide action bar icons
                supportInvalidateOptionsMenu();

            }
        };


        drawerLayout.setDrawerListener(mDrawerToggle);


        menuBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //if(position == 1 || position == 2)
                  //  viewDefault=true;

                if (position > 0)
                    displayView(position);


            }
        });


    }

    private void displayView(int position) {
        fragment = null;

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        System.out.println("Ramesh menu bar position :" + position);
        if (prevPosition > 0 && !settingSelected)
        {
            searchView.setQuery("", true);
            if(inboxSelected) {
                InBoxFragment inBoxFragment = (InBoxFragment) getFragmentManager().findFragmentByTag(menuTitles[prevPosition]);
                inBoxFragment.stopMediaPlayerIfPlaying();
                inBoxFragment.dataBase.closeDatabase();
            }
            else
            {
                TrashFragment trashFragment = (TrashFragment) getFragmentManager().findFragmentByTag(menuTitles[prevPosition]);
                trashFragment.stopMediaPlayerIfPlaying();
                trashFragment.dataBase.closeDatabase();

            }
    }

        settingSelected = false;
        this.position=position;
        switch (position) {

            case 1:


                fragment = new InBoxFragment();
                if(viewDefault) {
                    bundle.putString("MenuItem", menuTitles[defaultView]);

                    fragmentTransaction.replace(R.id.frameLayout, fragment, menuTitles[defaultView]);
                }
                else {
                    bundle.putString("MenuItem", menuTitles[position]);

                    fragmentTransaction.replace(R.id.frameLayout, fragment, menuTitles[position]);
                }

                fragment.setArguments(bundle);
                fragmentTransaction.commit();
                inboxSelected = true;

                break;
            case 2:
                inboxSelected = false;


                fragment = new TrashFragment();
                if(defaultView == 1)
                {
                    bundle.putString("MenuItem", menuTitles[position]);
                    fragment.setArguments(bundle);

                    fragmentTransaction.replace(R.id.frameLayout, fragment, menuTitles[position]);
                    fragmentTransaction.commit();
                }
                else
                {
                    if(viewDefault)
                    {
                        bundle.putString("MenuItem", menuTitles[defaultView]);

                        fragmentTransaction.replace(R.id.frameLayout, fragment, menuTitles[defaultView]);
                    }
                    else
                    {
                        bundle.putString("MenuItem", menuTitles[position]);

                        fragmentTransaction.replace(R.id.frameLayout, fragment, menuTitles[position]);


                    }
                    fragment.setArguments(bundle);
                    fragmentTransaction.commit();
                }


                break;

            case 3:
                if (inboxSelected) {
                    fragment = new InBoxFragment();
                    bundle.putString("MenuItem", menuTitles[position]);
                    fragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.frameLayout, fragment, menuTitles[position]);
                    fragmentTransaction.commit();

                    inboxSelected = true;
                } else {

                    fragment = new TrashFragment();

                    bundle.putString("MenuItem", menuTitles[position]);
                    fragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.frameLayout, fragment, menuTitles[position]);
                    fragmentTransaction.commit();
                    inboxSelected = false;

                }


                break;

            case 4:
                if (inboxSelected) {

                    fragment = new InBoxFragment();
                    bundle.putString("MenuItem", menuTitles[position]);
                    fragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.frameLayout, fragment, menuTitles[position]);
                    fragmentTransaction.commit();

                    inboxSelected=true;
                } else
                {

                    fragment = new TrashFragment();

                    bundle.putString("MenuItem", menuTitles[position]);
                    fragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.frameLayout, fragment, menuTitles[position]);
                    fragmentTransaction.commit();
                    inboxSelected = false;




                }



                break;



            case 5:
                if (inboxSelected) {
                    fragment = new InBoxFragment();
                    bundle.putString("MenuItem", menuTitles[position]);
                    fragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.frameLayout, fragment, menuTitles[position]);
                    fragmentTransaction.commit();

                    inboxSelected = true;
                } else {

                    fragment = new TrashFragment();

                    bundle.putString("MenuItem", menuTitles[position]);
                    fragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.frameLayout, fragment, menuTitles[position]);
                    fragmentTransaction.commit();
                    inboxSelected = false;

                }


                break;
            case 6:

                settingSelected=true;
                Intent settingsIntent = new Intent(this,SettingsActivity.class);
                startActivity(settingsIntent);
                break;

            default:


    }



        menuBar.setItemChecked(position, true);
        if(!settingSelected)
        {
        if(position == 1 || position==2) {
            if(position ==2 &&  defaultView == 1)
            prevPosition = position;
            else
            {
                if(viewDefault)
                prevPosition = defaultView;
                else
                    prevPosition=position;
            }
        }
        else
            prevPosition=position;


        }
       // if(!settingSelected )
        //setTitle(menuTitles[position]);
        drawerLayout.closeDrawer(menuBar);

    }

    @Override
    protected void onResume() {


            System.out.println("Ramesh OnResume " + prevPosition);
        String defaultViewString=PreferenceManager.getDefaultSharedPreferences(this).getString("ViewList", "3");
        defaultView=Integer.parseInt(defaultViewString);


        if((previousSateOnStop && !settingSelected) )
        {
            Bundle bundle = new Bundle();
            bundle.putBoolean("Password", true);
            android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
            commonDialogFragment.setCancelable(false);
            commonDialogFragment.setArguments(bundle);
            commonDialogFragment.show(this.getFragmentManager(), "CommonDialog");



        }

            if (settingSelected || (!firstTimeFlag && inboxSelected))
            {
                if(settingSelected) {

                    displayView(prevPosition);

                }
                else
                {
                    viewDefault=true;
                    displayView(1);
                }

                if(previousMenuItem != null)
                    previousMenuItem.setVisible(true);

                currentViewSetInvisible(menu);
                initializeOverflowMenu(menu);


            }



        firstTimeFlag=false;

        super.onResume();
    }

    @Override
    protected void onStop() {

        previousSateOnStop=true;
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu=menu;

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        //defaultViewSetInvisible(menu);
        currentViewSetInvisible(menu);
        initializeOverflowMenu(menu);


        //searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchItem);
        System.out.println("Ramesh onCreateOptionsMenu");
        // Configure the search info and add any event listeners
        SearchManager searchManager= (SearchManager)getSystemService(getApplicationContext().SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchableInfo);

        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                System.out.println("Ramesh query :" + query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                System.out.println("Ramesh query text Chage :" + newText);

                if (!newText.equals("") || (newText.equals("") && !prevText.equals(""))) {
                    System.out.println("Ramesh query text Chage :" + newText);
                    if (!settingSelected) {
                        if (inboxSelected) {

                            InBoxFragment inBoxFragment = (InBoxFragment) getFragmentManager().findFragmentByTag(menuTitles[prevPosition]);
                            inBoxFragment.showMatchingRecords(newText);

                        } else {
                            TrashFragment trashFragment = (TrashFragment) getFragmentManager().findFragmentByTag(menuTitles[prevPosition]);
                            trashFragment.showMatchingRecords(newText);

                        }

                    }
                }


                prevText = newText;


                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        menu.findItem(R.id.sort_by_date).setVisible(true);
        menu.findItem(R.id.sort_by_sender).setVisible(true);
        menu.findItem(R.id.view_by_date).setVisible(true);
        menu.findItem(R.id.view_by_month).setVisible(true);

        int id = item.getItemId();
        if(!(id == R.id.action_settings))
        {
            item.setVisible(false);
            previousMenuItem=item;

        }

        viewDefault=false;

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            displayView(6);
            return true;
        }
        if (id == R.id.drawer_icon) {
            OnDrawerIconClicked();
            return true;
        }
        if(id == R.id.sort_by_date)
        {

            if(inboxSelected)
            displayView(1);
            else
            displayView(2);
            return true;
        }
        if(id == R.id.sort_by_sender)
        {
            displayView(3);
            return true;
        }

        if(id == R.id.view_by_date)
        {
            displayView(4);
            return true;
        }


        if(id == R.id.view_by_month)
        {
            displayView(5);
            return true;
        }
        if(id == R.id.inbox)
        {
            viewDefault=true;
            defaultViewSetInvisible(menu);
            displayView(1);
            menu.findItem(R.id.inbox).setVisible(false);
            menu.findItem(R.id.trash).setVisible(true);

            return true;
        }

        if(id == R.id.trash)
        {
            defaultViewSetInvisible(menu);
            viewDefault=true;
            displayView(2);
            menu.findItem(R.id.inbox).setVisible(true);
            menu.findItem(R.id.trash).setVisible(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void defaultViewSetInvisible(Menu menu ) {
        System.out.println("Ramesh defaultView set Invisible");

        switch (defaultView) {
            case 1:
                menu.findItem(R.id.sort_by_date).setVisible(false);

                break;
            case 3:
                menu.findItem(R.id.sort_by_sender).setVisible(false);
                break;
            case 4:
                menu.findItem(R.id.view_by_date).setVisible(false);
                break;
            case 5:
                menu.findItem(R.id.view_by_month).setVisible(false);
                break;

        }
    }

    public void initializeOverflowMenu(Menu menu)
    {
        if(prevPosition > 2)
        {
            if(inboxSelected)
            {
                menu.findItem(R.id.trash).setVisible(true);
                menu.findItem(R.id.inbox).setVisible(false);


            }
            else
            {
                System.out.println("Ramesh initialize Overflow Menu");
                menu.findItem(R.id.trash).setVisible(false);
                menu.findItem(R.id.inbox).setVisible(true);

            }



        }



    }

    public void currentViewSetInvisible(Menu menu )
    {
        System.out.println("Ramesh currentViewsetInvisible");

        switch (prevPosition)
        {
            case 1:
                menu.findItem(R.id.sort_by_date).setVisible(false);
                previousMenuItem=menu.findItem(R.id.sort_by_date);

                menu.findItem(R.id.trash).setVisible(true);
                menu.findItem(R.id.inbox).setVisible(false);

                break;
            case 2 :
                menu.findItem(R.id.sort_by_date).setVisible(false);
                previousMenuItem=menu.findItem(R.id.sort_by_date);
                menu.findItem(R.id.trash).setVisible(false);
                menu.findItem(R.id.inbox).setVisible(true);
                break;

            case 3:
                menu.findItem(R.id.sort_by_sender).setVisible(false);
                previousMenuItem=menu.findItem(R.id.sort_by_sender);

                break;
            case 4:
                menu.findItem(R.id.view_by_date).setVisible(false);
                previousMenuItem=menu.findItem(R.id.view_by_date);
                break;
            case 5:
                menu.findItem(R.id.view_by_month).setVisible(false);
                previousMenuItem=menu.findItem(R.id.view_by_month);
                break;

        }

    }



    @Override
    public void onFragmentInteraction(Uri uri) {
        System.out.println("Ramesh in onFragment Interaction");
    }

    @Override
    public void OnDrawerIconClicked() {
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    @Override
    public void toInitialiseSearchQuery() {

        searchView.setQuery("",true);
        searchView.setIconified(true);


    }

    @Override
    public void updateActionBarTitle(int i) {
        System.out.println("Ramesh update Action Bar Title :" + i);
        if(inboxSelected)
        setTitle("    " + "INBOX(" + i + ")");
        else
            setTitle("    " + "TRASH(" + i + ")");
    }


    @Override
    public void onConfirmDelete() {
        System.out.println("Ramesh onConfirmDelete");

        InBoxFragment inBoxfragment= (InBoxFragment) getFragmentManager().findFragmentByTag(menuTitles[prevPosition]);
        inBoxfragment.onConfirmDelete();


    }

    @Override
    public void onConfirmEmptyInbox() {

        System.out.println("Ramesh onConfirmEmpty");
        InBoxFragment inBoxfragment= (InBoxFragment) getFragmentManager().findFragmentByTag(menuTitles[prevPosition]);
        inBoxfragment.onConfirmEmptyInbox();

    }

    @Override
    public void onConfirmTrashDelete() {

        Log.d("Ramesh"," onConfirm TrashDelete");
        TrashFragment trashFragment= (TrashFragment) getFragmentManager().findFragmentByTag(menuTitles[prevPosition]);
        trashFragment.onConfirmTrashDelete();


    }

    @Override
    public void onConfirmTrashEmpty() {

        Log.d("Ramesh", "onConfrim Trash Empty");
        TrashFragment trashFragment= (TrashFragment) getFragmentManager().findFragmentByTag(menuTitles[prevPosition]);
        trashFragment.onConfirmEmptyTrash();


    }

    @Override
    public void checkPassword(String password) {

            SharedPreferences passwordSP= getSharedPreferences("AppPassword",MODE_PRIVATE);
            String appPassword =passwordSP.getString("AppPassword", null);
        if(!password.equals(appPassword))
        {
            Bundle bundle = new Bundle();
            bundle.putBoolean("Password", true);
            android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
            commonDialogFragment.setCancelable(false);
            commonDialogFragment.setArguments(bundle);

            commonDialogFragment.show(this.getFragmentManager(), "CommonDialog");
        }
        else
        {

         //   if(settingSelected || (!firstTimeFlag && inboxSelected))
           //     displayView(prevPosition);
            previousSateOnStop=false;

        }



    }

    public void passwordSetWhenAppLaunched()

    {

        Bundle bundle = new Bundle();
        bundle.putBoolean("setPassword", true);
        android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
        commonDialogFragment.setCancelable(false);
        commonDialogFragment.setArguments(bundle);

        commonDialogFragment.show(this.getFragmentManager(), "CommonDialog");

        bundle = new Bundle();
        bundle.putBoolean("setEmail", true);
         commonDialogFragment = new CommonDialogFragment();
        commonDialogFragment.setCancelable(false);
        commonDialogFragment.setArguments(bundle);

        commonDialogFragment.show(this.getFragmentManager(), "CommonDialog");
    }

    @Override
    public void setPassword(String setPassword) {

        SharedPreferences forPassowrd = getSharedPreferences("AppPassword",MODE_PRIVATE);
        SharedPreferences.Editor forPasswordEditor = forPassowrd.edit();
        forPasswordEditor.putString("AppPassword",setPassword);
        forPasswordEditor.commit();







    }

    @Override
    public void setEmail(String Email) {
        SharedPreferences forPassowrd = getSharedPreferences("AppPassword",MODE_PRIVATE);
        SharedPreferences.Editor forPasswordEditor = forPassowrd.edit();
        forPasswordEditor.putString("EmailId",Email);
        forPasswordEditor.commit();

    }

    @Override
    public void getEmail() {
        SharedPreferences appPassword = getSharedPreferences("AppPassword", MODE_PRIVATE);
        String emailId=appPassword.getString("EmailId", null);
        Bundle bundle = new Bundle();
        bundle.putBoolean("displayMessage", true);
        bundle.putString("EmailId", emailId);
        CommonDialogFragment commonDialogFragment = new CommonDialogFragment();
        commonDialogFragment.setCancelable(false);
        commonDialogFragment.setArguments(bundle);

        commonDialogFragment.show(this.getFragmentManager(), "CommonDialog");

    }

    @Override
    public void setDate(long date) {

    }

    @Override
    public void sendEmail(String EmailId) {

        SharedPreferences appPassword = getSharedPreferences("AppPassword",MODE_PRIVATE);
        String password=appPassword.getString("AppPassword", null);
        Intent emailIntet = new Intent(Intent.ACTION_SEND);
        emailIntet.setType("message/rfc822");
        emailIntet.putExtra(Intent.EXTRA_EMAIL, new String[]{EmailId});
        emailIntet.putExtra(Intent.EXTRA_SUBJECT, "CallRecorder App Password");
        emailIntet.putExtra(Intent.EXTRA_TEXT, "Password :" + password);
        try {
            startActivity(Intent.createChooser(emailIntet, "Send mail..."));
            //Toast.makeText(this, "Password has been sent", Toast.LENGTH_SHORT).show();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }

    }

}

