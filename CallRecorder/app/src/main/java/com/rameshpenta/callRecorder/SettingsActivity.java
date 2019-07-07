package com.rameshpenta.callRecorder;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements CommonDialogFragment.OnFragmentInteractionListener,CommonDialogFragment.DeleteOnClickListener {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    boolean isSdCardPresent;
    ListPreference listPreference;
    Preference setDatePreference;

    String dateText;


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_callrecorder);
         isSdCardPresent=Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final CheckBoxPreference checkBoxPreferenceSD = (CheckBoxPreference) findPreference("SD");
        final CheckBoxPreference checkBoxPreferencePhone = (CheckBoxPreference) findPreference("Phone");
            Preference preference=findPreference("ChangePassword");
             setDatePreference = findPreference("setDate");
         listPreference = (ListPreference) findPreference("ViewList");
        listPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                listPreference.setSummary("Default View :" + listPreference.getEntry().toString());
                return false;
            }
        });

        setDatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("DatePicker", true);
                android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
                commonDialogFragment.setCancelable(true);
                commonDialogFragment.setArguments(bundle);

                commonDialogFragment.show(SettingsActivity.this.getFragmentManager(), "CommonDialog");


                return false;
            }
        });
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("setPassword", true);
                android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
                commonDialogFragment.setCancelable(false);
                commonDialogFragment.setArguments(bundle);

                commonDialogFragment.show(SettingsActivity.this.getFragmentManager(), "CommonDialog");


                return false;
            }
        });


        checkBoxPreferenceSD.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (!isSdCardPresent) {
                    checkBoxPreferencePhone.setChecked(true);
                    checkBoxPreferenceSD.setChecked(false);
                    Toast.makeText(SettingsActivity.this, "SD card is not Available", Toast.LENGTH_LONG);
                } else if (checkBoxPreferenceSD.isChecked()) {
                    checkBoxPreferencePhone.setChecked(false);
                    checkBoxPreferenceSD.setChecked(true);
                }


                return false;
            }
        });

        checkBoxPreferencePhone.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (checkBoxPreferencePhone.isChecked())
                    checkBoxPreferenceSD.setChecked(false);
                return false;
            }
        });

    }

    @Override
    protected void onResume() {

        listPreference.setSummary("Default View :" + listPreference.getEntry().toString());
        listPreference.getSharedPreferences().registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                listPreference.setSummary("Default View :" + listPreference.getEntry().toString());
            }
        });

        if (setDatePreference.getSharedPreferences().getLong("setDate", 0) == 0)
        {
            setDatePreference.setSummary("Show Call Records After  " + "99/99/99");


        } else {
            dateText = Long.valueOf(setDatePreference.getSharedPreferences().getLong("setDate", 0)).toString();

            dateText = dateText.substring(4, 6) + "/" + dateText.substring(6, 8) + "/" + dateText.substring(0, 4);
            setDatePreference.setSummary("Show Call Records After  " + dateText);
        }

        setDatePreference.getSharedPreferences().registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (setDatePreference.getSharedPreferences().getLong("setDate", 0) == 0)
                {
                    setDatePreference.setSummary("Show Call Records After  " + "99/99/99");


                } else {
                    dateText = Long.valueOf(setDatePreference.getSharedPreferences().getLong("setDate", 0)).toString();

                    dateText = dateText.substring(4, 6) + "/" + dateText.substring(6, 8) + "/" + dateText.substring(0, 4);
                    setDatePreference.setSummary("Show Call Records After  " + dateText);
                }

            }
        });
        super.onResume();
    }

    @Override
    protected void onPause() {
        listPreference.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            }
        });

        setDatePreference.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            }
        });
        super.onPause();
    }



    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("example_text"));
        bindPreferenceSummaryToValue(findPreference("example_list"));
        bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        bindPreferenceSummaryToValue(findPreference("sync_frequency"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    @Override
    public void onConfirmDelete() {

    }

    @Override
    public void onConfirmEmptyInbox() {

    }

    @Override
    public void onConfirmTrashDelete() {

    }

    @Override
    public void onConfirmTrashEmpty() {

    }

    @Override
    public void checkPassword(String password) {

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

    }

    @Override
    public void getEmail() {

    }


    @Override
    public void setDate(long date) {

        Calendar calendar = Calendar.getInstance();
        Date currentTime =calendar.getTime();


        String sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").format(currentTime);


        String callDate=sd.replace("-","").replace("T","").replace(":", "").substring(0, 8);

        System.out.println("Ramesh Today Date :" + callDate);

        long today = Long.parseLong(callDate);
        if(date > today )
        {

            Bundle bundle = new Bundle();
            bundle.putBoolean("DisplayDateMessage", true);
            android.app.DialogFragment commonDialogFragment = new CommonDialogFragment();
            commonDialogFragment.setCancelable(true);
            commonDialogFragment.setArguments(bundle);

            commonDialogFragment.show(SettingsActivity.this.getFragmentManager(), "CommonDialog");




        }

            else
        {




            if(date == 0)
            {
                setDatePreference.getSharedPreferences().edit().putLong("setDate", date).commit();
                setDatePreference.setSummary("Show call Records After  " + "99/99/9999");
            }
            else
            {
                 dateText =Long.valueOf(date).toString();

                dateText   =  dateText.substring(4,6) + "/" + dateText.substring(6,8)+"/"+dateText.substring(0,4);
                setDatePreference.getSharedPreferences().edit().putLong("setDate", date).commit();
                setDatePreference.setSummary("Show Call Records After   " + dateText);

            }

        }

    }

    @Override
    public void sendEmail(String EmailId) {

    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"));
            bindPreferenceSummaryToValue(findPreference("example_list"));
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }
}
