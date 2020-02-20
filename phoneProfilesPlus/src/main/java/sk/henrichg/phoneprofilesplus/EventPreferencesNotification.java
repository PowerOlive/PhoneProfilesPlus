package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.List;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.WorkManager;

class EventPreferencesNotification extends EventPreferences {

    String _applications;
    boolean _inCall;
    boolean _missedCall;
    int _duration;
    boolean _checkContacts;
    String _contacts;
    String _contactGroups;
    int _contactListType;
    boolean _checkText;
    String _text;

    static final String PREF_EVENT_NOTIFICATION_ENABLED = "eventNotificationEnabled";
    private static final String PREF_EVENT_NOTIFICATION_APPLICATIONS = "eventNotificationApplications";
    private static final String PREF_EVENT_NOTIFICATION_IN_CALL = "eventNotificationInCall";
    private static final String PREF_EVENT_NOTIFICATION_MISSED_CALL = "eventNotificationMissedCall";
    static final String PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS = "eventNotificationNotificationsAccessSettings";
    private static final String PREF_EVENT_NOTIFICATION_DURATION = "eventNotificationDuration";
    private static final String PREF_EVENT_NOTIFICATION_CHECK_CONTACTS = "eventNotificationCheckContacts";
    private static final String PREF_EVENT_NOTIFICATION_CHECK_TEXT = "eventNotificationCheckText";
    private static final String PREF_EVENT_NOTIFICATION_CONTACT_GROUPS = "eventNotificationContactGroups";
    private static final String PREF_EVENT_NOTIFICATION_CONTACTS = "eventNotificationContacts";
    private static final String PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE = "eventNotificationContactListType";
    private static final String PREF_EVENT_NOTIFICATION_TEXT = "eventNotificationText";

    private static final String PREF_EVENT_NOTIFICATION_CATEGORY = "eventNotificationCategoryRoot";

    EventPreferencesNotification(Event event,
                                        boolean enabled,
                                        String applications,
                                        boolean inCall,
                                        boolean missedCall,
                                        int duration,
                                        boolean checkContacts,
                                        String contactGroups,
                                        String contacts,
                                        boolean checkText,
                                        String text,
                                        int contactListType
                                 )
    {
        super(event, enabled);

        this._applications = applications;
        this._inCall = inCall;
        this._missedCall = missedCall;
        this._duration = duration;
        this._checkContacts = checkContacts;
        this._contacts = contacts;
        this._contactGroups = contactGroups;
        this._checkText = checkText;
        this._text = text;
        this._contactListType = contactListType;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesNotification._enabled;
        this._applications = fromEvent._eventPreferencesNotification._applications;
        this._inCall = fromEvent._eventPreferencesNotification._inCall;
        this._missedCall = fromEvent._eventPreferencesNotification._missedCall;
        this._duration = fromEvent._eventPreferencesNotification._duration;
        this._checkContacts = fromEvent._eventPreferencesNotification._checkContacts;
        this._contacts = fromEvent._eventPreferencesNotification._contacts;
        this._contactGroups = fromEvent._eventPreferencesNotification._contactGroups;
        this._checkText = fromEvent._eventPreferencesNotification._checkText;
        this._text = fromEvent._eventPreferencesNotification._text;
        this._contactListType = fromEvent._eventPreferencesNotification._contactListType;

        this.setSensorPassed(fromEvent._eventPreferencesNotification.getSensorPassed());
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_NOTIFICATION_ENABLED, _enabled);
            editor.putString(PREF_EVENT_NOTIFICATION_APPLICATIONS, this._applications);
            editor.putBoolean(PREF_EVENT_NOTIFICATION_IN_CALL, this._inCall);
            editor.putBoolean(PREF_EVENT_NOTIFICATION_MISSED_CALL, this._missedCall);
            editor.putString(PREF_EVENT_NOTIFICATION_DURATION, String.valueOf(this._duration));
            editor.putBoolean(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS, this._checkContacts);
            editor.putString(PREF_EVENT_NOTIFICATION_CONTACTS, this._contacts);
            editor.putString(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS, this._contactGroups);
            editor.putBoolean(PREF_EVENT_NOTIFICATION_CHECK_TEXT, this._checkText);
            editor.putString(PREF_EVENT_NOTIFICATION_TEXT, this._text);
            editor.putString(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE, String.valueOf(this._contactListType));
            editor.apply();
        //}
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this._enabled = preferences.getBoolean(PREF_EVENT_NOTIFICATION_ENABLED, false);
            this._applications = preferences.getString(PREF_EVENT_NOTIFICATION_APPLICATIONS, "");
            this._inCall = preferences.getBoolean(PREF_EVENT_NOTIFICATION_IN_CALL, false);
            this._missedCall = preferences.getBoolean(PREF_EVENT_NOTIFICATION_MISSED_CALL, false);
            this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_NOTIFICATION_DURATION, "0"));
            this._checkContacts = preferences.getBoolean(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS, false);
            this._contacts = preferences.getString(PREF_EVENT_NOTIFICATION_CONTACTS, "");
            this._contactGroups = preferences.getString(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS, "");
            this._checkText = preferences.getBoolean(PREF_EVENT_NOTIFICATION_CHECK_TEXT, false);
            this._text = preferences.getString(PREF_EVENT_NOTIFICATION_TEXT, "");
            this._contactListType = Integer.parseInt(preferences.getString(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE, "0"));
        //}
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_notification_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_NOTIFICATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_notifications), addPassStatus, DatabaseHandler.ETYPE_NOTIFICATION, context);
                    descr = descr + "</b> ";
                }

                if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context)) {
                    descr = descr + "* " + context.getString(R.string.event_preferences_notificationsAccessSettings_disabled_summary) + "! *";
                } else {
                    //descr = descr + context.getString(R.string.event_preferences_notificationsAccessSettings_enabled_summary) + "<br>";

                    if (this._inCall) {
                        descr = descr + "<b>" + context.getString(R.string.event_preferences_notifications_inCall) + "</b>";
                    }
                    if (this._missedCall) {
                        if (this._inCall)
                            descr = descr + " • ";
                        descr = descr + "<b>" +context.getString(R.string.event_preferences_notifications_missedCall) + "</b>";
                    }
                    String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                    if (!this._applications.isEmpty() && !this._applications.equals("-")) {
                        String[] splits = this._applications.split("\\|");
                        if (splits.length == 1) {
                            String packageName = Application.getPackageName(splits[0]);
                            String activityName = Application.getActivityName(splits[0]);
                            PackageManager packageManager = context.getPackageManager();
                            if (activityName.isEmpty()) {
                                ApplicationInfo app;
                                try {
                                    app = packageManager.getApplicationInfo(packageName, 0);
                                    if (app != null)
                                        selectedApplications = packageManager.getApplicationLabel(app).toString();
                                } catch (Exception e) {
                                    selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
                                }
                            } else {
                                Intent intent = new Intent();
                                intent.setClassName(packageName, activityName);
                                ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                                if (info != null)
                                    selectedApplications = info.loadLabel(packageManager).toString();
                            }
                        } else
                            selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
                    }
                    if (this._inCall || this._missedCall)
                        descr = descr + " • ";
                    descr = descr + /*"(S) "+*/context.getString(R.string.event_preferences_notifications_applications) + ": <b>" + selectedApplications + "</b>";

                    if (this._checkContacts) {
                        descr = descr + " • ";
                        descr = descr + "<b>" + context.getString(R.string.event_preferences_notifications_checkContacts) + "</b>: ";

                        descr = descr + context.getString(R.string.event_preferences_notifications_contact_groups) + ": ";
                        descr = descr + "<b>" + ContactGroupsMultiSelectDialogPreferenceX.getSummary(_contactGroups, context) + "</b> • ";

                        descr = descr + context.getString(R.string.event_preferences_notifications_contacts) + ": ";
                        descr = descr + "<b>" + ContactsMultiSelectDialogPreferenceX.getSummary(_contacts, true, context) + "</b> • ";

                        descr = descr + context.getString(R.string.event_preferences_contactListType) + ": ";
                        String[] contactListTypes = context.getResources().getStringArray(R.array.eventNotificationContactListTypeArray);
                        descr = descr + "<b>" + contactListTypes[this._contactListType] + "</b>";
                    }
                    if (this._checkText) {
                        descr = descr + " • ";
                        descr = descr + "<b>" + context.getString(R.string.event_preferences_notifications_checkText) + "</b>: ";

                        descr = descr + context.getString(R.string.event_preferences_notifications_text) + ": ";
                        descr = descr + "<b>" + _text + "</b>";
                    }
                    descr = descr + " • ";
                    descr = descr + context.getString(R.string.pref_event_duration) + ": <b>" + GlobalGUIRoutines.getDurationString(this._duration) + "</b>";
                }
            }
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_NOTIFICATION_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary = context.getString(R.string.event_preferences_volumeNotificationsAccessSettings_summary);
                if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext())) {
                    summary = "* " + context.getString(R.string.event_preferences_notificationsAccessSettings_disabled_summary) + "! *\n\n"+
                            summary;
                }
                else {
                    summary = context.getString(R.string.event_preferences_notificationsAccessSettings_enabled_summary) + ".\n\n"+
                            summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 0;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 0, false, false, false);
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE)) {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACTS);
            if (preference != null) {
                preference.setEnabled(value.equals("true"));
            }
            preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS);
            if (preference != null) {
                preference.setEnabled(value.equals("true"));
            }
            preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE);
            if (preference != null) {
                preference.setEnabled(value.equals("true"));
            }
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_CHECK_TEXT)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_TEXT);
            if (preference != null) {
                preference.setEnabled(value.equals("true"));
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesNotification.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesNotification.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_NOTIFICATION_ENABLED, false);
        SwitchPreferenceCompat switchPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_IN_CALL);
        if (switchPreference != null) {
            GlobalGUIRoutines.setPreferenceTitleStyleX(switchPreference, enabled, preferences.getBoolean(PREF_EVENT_NOTIFICATION_IN_CALL, false), true, !isRunnable, false);
        }
        switchPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_MISSED_CALL);
        if (switchPreference != null) {
            GlobalGUIRoutines.setPreferenceTitleStyleX(switchPreference, enabled, preferences.getBoolean(PREF_EVENT_NOTIFICATION_MISSED_CALL, false), true, !isRunnable, false);
        }
        Preference applicationsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_APPLICATIONS);
        if (applicationsPreference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_NOTIFICATION_APPLICATIONS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(applicationsPreference, enabled, bold, true, !isRunnable, false);
        }
        Preference checkContactsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS);
        if ((checkContactsPreference != null) && prefMng.getSharedPreferences().getBoolean(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS, false)) {
            Preference _preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS);
            if (_preference != null) {
                boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS, "").isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(_preference, enabled, bold, false, !isRunnable, false);
            }
            _preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACTS);
            if (_preference != null) {
                boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_NOTIFICATION_CONTACTS, "").isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(_preference, enabled, bold, false, !isRunnable, false);
            }
            _preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE);
            if (_preference != null) {
                boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE, "").isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(_preference, enabled, bold, false, !isRunnable, false);
            }
        }
        Preference checkTextPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CHECK_TEXT);
        if ((checkTextPreference != null) && prefMng.getSharedPreferences().getBoolean(PREF_EVENT_NOTIFICATION_CHECK_TEXT, false)) {
            Preference _preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_TEXT);
            if (_preference != null) {
                boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_NOTIFICATION_TEXT, "").isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(_preference, enabled, bold, false, !isRunnable, false);
            }
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_NOTIFICATION_ENABLED) ||
            key.equals(PREF_EVENT_NOTIFICATION_IN_CALL) ||
            key.equals(PREF_EVENT_NOTIFICATION_MISSED_CALL) ||
            key.equals(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS) ||
            key.equals(PREF_EVENT_NOTIFICATION_CHECK_TEXT)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_APPLICATIONS)||
            key.equals(PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS) ||
            key.equals(PREF_EVENT_NOTIFICATION_DURATION) ||
            key.equals(PREF_EVENT_NOTIFICATION_CONTACTS) ||
            key.equals(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS) ||
            key.equals(PREF_EVENT_NOTIFICATION_TEXT) ||
            key.equals(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_IN_CALL, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_MISSED_CALL, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_APPLICATIONS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_DURATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_CHECK_CONTACTS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_CONTACTS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_CONTACT_GROUPS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_CHECK_TEXT, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_TEXT, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_NOTIFICATION_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesNotification tmp = new EventPreferencesNotification(this._event, this._enabled,
                                                        this._applications, this._inCall, this._missedCall, this._duration,
                                                        this._checkContacts, this._contactGroups, this._contacts,
                                                        this._checkText, this._text, this._contactListType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_NOTIFICATION_ENABLED, false);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    public boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        runnable = runnable && (_inCall || _missedCall || (!_applications.isEmpty()));

        return runnable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            boolean enabled = PPNotificationListenerService.isNotificationListenerServiceEnabled(context);
            ApplicationsMultiSelectDialogPreferenceX applicationsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_APPLICATIONS);
            Preference ringingCallPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_IN_CALL);
            Preference missedCallPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_MISSED_CALL);
            SwitchPreferenceCompat checkContactsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CHECK_CONTACTS);
            Preference contactGroupsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACT_GROUPS);
            Preference contactsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACTS);
            SwitchPreferenceCompat checkTextPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CHECK_TEXT);
            Preference textPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_TEXT);
            Preference contactListTypePreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CONTACT_LIST_TYPE);

            if (applicationsPreference != null) {
                applicationsPreference.setEnabled(enabled);
                applicationsPreference.setSummaryAMSDP();
            }
            if (ringingCallPreference != null) {
                ringingCallPreference.setEnabled(enabled);
            }
            if (missedCallPreference != null) {
                missedCallPreference.setEnabled(enabled);
            }
            if (checkContactsPreference != null) {
                checkContactsPreference.setEnabled(enabled);
            }
            if (contactGroupsPreference != null) {
                boolean checkEnabled = (checkContactsPreference != null) && (checkContactsPreference.isChecked());
                contactGroupsPreference.setEnabled(enabled && checkEnabled);
            }
            if (contactsPreference != null) {
                boolean checkEnabled = (checkContactsPreference != null) && (checkContactsPreference.isChecked());
                contactsPreference.setEnabled(enabled && checkEnabled);
            }
            if (contactListTypePreference != null) {
                boolean checkEnabled = (checkContactsPreference != null) && (checkContactsPreference.isChecked());
                contactListTypePreference.setEnabled(enabled && checkEnabled);
            }
            if (checkTextPreference != null) {
                checkTextPreference.setEnabled(enabled);
            }
            if (textPreference != null) {
                boolean checkEnabled = (checkTextPreference != null) && (checkTextPreference.isChecked());
                textPreference.setEnabled(enabled && checkEnabled);
            }

            SharedPreferences preferences = prefMng.getSharedPreferences();
            setSummary(prefMng, PREF_EVENT_NOTIFICATION_NOTIFICATION_ACCESS, preferences, context);
            setCategorySummary(prefMng, preferences, context);
        /*}
        else {
            PreferenceScreen preferenceScreen = (PreferenceScreen) prefMng.findPreference("eventPreferenceScreen");
            PreferenceScreen preferenceCategory = (PreferenceScreen) prefMng.findPreference("eventNotificationCategory");
            if ((preferenceCategory != null) && (preferenceScreen != null))
                preferenceScreen.removePreference(preferenceCategory);
        }*/
    }

    private long computeAlarm(Context context)
    {
        //PPApplication.logE("EventPreferencesNotification.computeAlarm","xxx");

        if (this._duration != 0) {
            StatusBarNotification newestNotification = getNewestVisibleNotification(context);
            if (newestNotification != null) {
                return newestNotification.getPostTime() + this._duration * 1000;
            }
        }
        return 0;
    }

    @Override
    public void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        //PPApplication.logE("EventPreferencesNotification.setSystemRunningEvent","xxx");

        removeAlarm(context);
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        //PPApplication.logE("EventPreferencesNotification.setSystemPauseEvent","xxx");

        removeAlarm(context);

        if (!(isRunnable(context) && _enabled))
            return;

        setAlarm(computeAlarm(context), context);
    }

    @Override
    public void removeSystemEvent(Context context)
    {
        removeAlarm(context);

        //PPApplication.logE("EventPreferencesNotification.removeSystemEvent", "xxx");
    }

    private void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, NotificationEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_NOTIFICATION_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, NotificationEventEndBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    //PPApplication.logE("EventPreferencesNotification.removeAlarm", "alarm found");

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception ignored) {}
        try {
            WorkManager workManager = WorkManager.getInstance(context);
            //workManager.cancelUniqueWork("elapsedAlarmsNotificationSensorWork_"+(int)_event._id);
            workManager.cancelAllWorkByTag("elapsedAlarmsNotificationSensorWork_"+(int)_event._id);
        } catch (Exception ignored) {}
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    private void setAlarm(long alarmTime, Context context)
    {
        if (alarmTime > 0) {
            /*if (PPApplication.logEnabled()) {
                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                String result = sdf.format(alarmTime);
                PPApplication.logE("EventPreferencesNotification.setAlarm", "endTime=" + result);
            }*/

            /*if (ApplicationPreferences.applicationUseAlarmClock(context)) {
                //Intent intent = new Intent(context, NotificationEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_NOTIFICATION_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, NotificationEventEndBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);

                }
            } else {
                Calendar now = Calendar.getInstance();
                long elapsedTime = (alarmTime + Event.EVENT_ALARM_TIME_OFFSET) - now.getTimeInMillis();

                if (PPApplication.logEnabled()) {
                    long allSeconds = elapsedTime / 1000;
                    long hours = allSeconds / 60 / 60;
                    long minutes = (allSeconds - (hours * 60 * 60)) / 60;
                    long seconds = allSeconds % 60;

                    PPApplication.logE("EventPreferencesNotification.setAlarm", "elapsedTime=" + hours + ":" + minutes + ":" + seconds);
                }

                Data workData = new Data.Builder()
                        .putString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK, ElapsedAlarmsWorker.ELAPSED_ALARMS_NOTIFICATION_EVENT_END_SENSOR)
                        .build();

                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(ElapsedAlarmsWorker.class)
                                .addTag("elapsedAlarmsNotificationSensorWork_"+(int)_event._id)
                                .setInputData(workData)
                                .setInitialDelay(elapsedTime, TimeUnit.MILLISECONDS)
                                .build();
                try {
                    WorkManager workManager = WorkManager.getInstance(context);
                    PPApplication.logE("[HANDLER] EventPreferencesNotification.setAlarm", "enqueueUniqueWork - elapsedTime="+elapsedTime);
                    //workManager.enqueueUniqueWork("elapsedAlarmsNotificationSensorWork_"+(int)_event._id, ExistingWorkPolicy.REPLACE, worker);
                    workManager.enqueue(worker);
                } catch (Exception ignored) {}
            }*/

            //Intent intent = new Intent(context, NotificationEventEndBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_NOTIFICATION_EVENT_END_BROADCAST_RECEIVER);
            //intent.setClass(context, NotificationEventEndBroadcastReceiver.class);

            //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (ApplicationPreferences.applicationUseAlarmClock) {
                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
                else {
                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    else //if (android.os.Build.VERSION.SDK_INT >= 19)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                }
            }
        }
    }


    // search if any configured package names are visible in status bar
    private StatusBarNotification isNotificationActive(StatusBarNotification[] statusBarNotifications, String packageName, boolean checkEnd/*, Context context*/) {
        for (StatusBarNotification statusBarNotification : statusBarNotifications) {
            String _packageName = statusBarNotification.getPackageName();

            boolean testText = false;
            if (_checkContacts)
                testText = true;
            if (_checkText)
                testText = true;

            String notificationTicker = "";
            String notificationTitle = "";
            String notificationText = "";
            if (testText) {
                testText = false;
                if (statusBarNotification.getNotification().tickerText != null) {
                    notificationTicker = statusBarNotification.getNotification().tickerText.toString();
                    testText = true;
                }
                Bundle extras = statusBarNotification.getNotification().extras;
                if (extras != null) {
                    if (extras.getString("android.title") != null) {
                        notificationTitle = extras.getString("android.title");
                        testText = true;
                    }
                    if (extras.getCharSequence("android.text") != null) {
                        notificationText = extras.getCharSequence("android.text").toString();
                        testText = true;
                    }
                }
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("EventPreferencesNotification.isNotificationActive", "notificationTicker=" + notificationTicker);
                    PPApplication.logE("EventPreferencesNotification.isNotificationActive", "notificationTitle=" + notificationTitle);
                    PPApplication.logE("EventPreferencesNotification.isNotificationActive", "notificationText=" + notificationText);
                }*/
            }

            boolean textFound = false;
            if (testText) {
                if (_checkContacts) {
                    if (_contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) {
                        boolean phoneNumberFound = false;
                        if (!notificationTitle.isEmpty())
                            phoneNumberFound = isContactConfigured(notificationTitle/*, context*/);
                        if (!notificationText.isEmpty() && (!phoneNumberFound))
                            phoneNumberFound = isContactConfigured(notificationText/*, context*/);
                        if (!notificationTicker.isEmpty() && (!phoneNumberFound))
                            phoneNumberFound = isContactConfigured(notificationTicker/*, context*/);

                        if (_contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_WHITE_LIST)
                            textFound = phoneNumberFound;
                        else
                            textFound = !phoneNumberFound;
                    }
                    else
                        textFound = true;
                }
                if (_checkText) {
                    String searchText = "";
                    for (int i = 0; i < 3; i++) {
                        if (i == 0) {
                            if (!notificationTitle.isEmpty())
                                searchText = notificationTitle;
                            else
                                continue;
                        }
                        if (i == 1) {
                            if (!notificationText.isEmpty())
                                searchText = notificationText;
                            else
                                continue;
                        }
                        if (i == 2) {
                            if (!notificationTicker.isEmpty())
                                searchText = notificationTicker;
                            else
                                continue;
                        }

                        String[] textSplits = _text.split("\\|");

                        String[] positiveList = new String[textSplits.length];
                        String[] negativeList = new String[textSplits.length];
                        int argsId;

                        // positive strings
                        boolean positiveExists = false;
                        argsId = 0;
                        for (String split : textSplits) {
                            if (!split.isEmpty()) {
                                String searchPattern = split;

                                if (searchPattern.startsWith("!")) {
                                    // only positive
                                    continue;
                                }

                                // when in searchPattern are not wildcards add %
                                if (!(searchPattern.contains("%") || searchPattern.contains("_")))
                                    searchPattern = "%" + searchPattern + "%";

                                searchPattern = searchPattern.replace("\\%", "{^^}");
                                searchPattern = searchPattern.replace("\\_", "[^^]");

                                searchPattern = searchPattern.replace("%", "(.*)");
                                searchPattern = searchPattern.replace("_", "(.)");

                                searchPattern = searchPattern.replace("{^^}", "\\%");
                                searchPattern = searchPattern.replace("[^^]", "\\_");

                                //if (!searchPattern.startsWith("(.*)"))
                                //    searchPattern = searchPattern + "^";
                                //if (!searchPattern.endsWith("(.*)"))
                                //    searchPattern = searchPattern + "$";

                                positiveList[argsId] = searchPattern;

                                positiveExists = true;

                                ++argsId;

                                /*if (PPApplication.logEnabled()) {
                                    PPApplication.logE("EventPreferencesNotification.isNotificationActive", "split=" + split);
                                    PPApplication.logE("EventPreferencesNotification.isNotificationActive", "searchPattern=" + searchPattern);
                                    PPApplication.logE("EventPreferencesNotification.isNotificationActive", "argsId=" + argsId);
                                }*/
                            }
                        }
                        //PPApplication.logE("EventPreferencesNotification.isNotificationActive", "positiveExists=" + positiveExists);

                        // negative strings
                        boolean negativeExists = false;
                        argsId = 0;
                        for (String split : textSplits) {
                            if (!split.isEmpty()) {
                                String searchPattern = split;

                                if (!searchPattern.startsWith("!")) {
                                    // only negative
                                    continue;
                                }

                                // remove !
                                searchPattern = searchPattern.substring(1);

                                // when in searchPattern are not wildcards add %
                                if (!(searchPattern.contains("%") || searchPattern.contains("_")))
                                    searchPattern = "%" + searchPattern + "%";

                                searchPattern = searchPattern.replace("\\%", "{^^}");
                                searchPattern = searchPattern.replace("\\_", "[^^]");

                                searchPattern = searchPattern.replace("%", "(.*)");
                                searchPattern = searchPattern.replace("_", "(.)");

                                searchPattern = searchPattern.replace("{^^}", "\\%");
                                searchPattern = searchPattern.replace("[^^]", "\\_");

                                //if (!searchPattern.startsWith("(.*)"))
                                //    searchPattern = searchPattern + "^";
                                //if (!searchPattern.endsWith("(.*)"))
                                //    searchPattern = searchPattern + "$";

                                negativeList[argsId] = searchPattern;

                                negativeExists = true;

                                ++argsId;

                                /*if (PPApplication.logEnabled()) {
                                    PPApplication.logE("EventPreferencesNotification.isNotificationActive", "split=" + split);
                                    PPApplication.logE("EventPreferencesNotification.isNotificationActive", "searchPattern=" + searchPattern);
                                    PPApplication.logE("EventPreferencesNotification.isNotificationActive", "argsId=" + argsId);
                                }*/
                            }
                        }
                        //PPApplication.logE("EventPreferencesNotification.isNotificationActive", "negativeExists=" + negativeExists);

                        boolean foundPositive = false;
                        if (positiveExists) {
                            for (String _positiveText : positiveList) {
                                if ((_positiveText != null) && (!_positiveText.isEmpty())) {
                                    /*if (PPApplication.logEnabled()) {
                                        PPApplication.logE("EventPreferencesNotification.isNotificationActive", "searchText.toLowerCase()=" + searchText.toLowerCase());
                                        PPApplication.logE("EventPreferencesNotification.isNotificationActive", "_positiveText.toLowerCase()=" + _positiveText.toLowerCase());
                                    }*/
                                    if (searchText.toLowerCase().matches(_positiveText.toLowerCase())) {
                                        foundPositive = true;
                                        break;
                                    }
                                }
                            }
                        }
                        boolean foundNegative = true;
                        if (negativeExists) {
                            for (String _negativeText : negativeList) {
                                if ((_negativeText != null) && (!_negativeText.isEmpty())) {
                                    /*if (PPApplication.logEnabled()) {
                                        PPApplication.logE("EventPreferencesNotification.isNotificationActive", "searchText.toLowerCase()=" + searchText.toLowerCase());
                                        PPApplication.logE("EventPreferencesNotification.isNotificationActive", "_negativeText.toLowerCase()=" + _negativeText.toLowerCase());
                                    }*/
                                    if (searchText.toLowerCase().matches(_negativeText.toLowerCase())) {
                                        foundNegative = false;
                                        break;
                                    }
                                }
                            }
                        }

                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("EventPreferencesNotification.isNotificationActive", "foundPositive=" + foundPositive);
                            PPApplication.logE("EventPreferencesNotification.isNotificationActive", "foundNegative=" + foundNegative);
                        }*/

                        textFound = foundPositive && foundNegative;

                        if (textFound)
                            break;
                    }

                }
            }
            //PPApplication.logE("EventPreferencesNotification.isNotificationActive", "textFound=" + textFound);

            if (testText && textFound) {
                if (checkEnd) {
                    if (_packageName.endsWith(packageName)) {
                        //PPApplication.logE("EventPreferencesNotification.isNotificationActive", "_packageName returned=" + _packageName);
                        return statusBarNotification;
                    }
                } else {
                    if (_packageName.equals(packageName)) {
                        //PPApplication.logE("EventPreferencesNotification.isNotificationActive", "_packageName returned=" + _packageName);
                        return statusBarNotification;
                    }
                }
            }
            else if (!testText) {
                if (checkEnd) {
                    if (_packageName.endsWith(packageName)) {
                        //PPApplication.logE("EventPreferencesNotification.isNotificationActive", "_packageName returned=" + _packageName);
                        return statusBarNotification;
                    }
                } else {
                    if (_packageName.equals(packageName)) {
                        //PPApplication.logE("EventPreferencesNotification.isNotificationActive", "_packageName returned=" + _packageName);
                        return statusBarNotification;
                    }
                }
            }
            else
                return null;
        }
        return null;
    }

    boolean isNotificationVisible(Context context) {
        //PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "xxx");
        if (PPNotificationListenerService.isNotificationListenerServiceEnabled(context)) {
            PPNotificationListenerService service = PPNotificationListenerService.getInstance();
            if (service != null) {
                try {
                    StatusBarNotification[] statusBarNotifications = service.getActiveNotifications();
                    StatusBarNotification notification;
                    if (this._inCall) {
                        // Nexus/Pixel??? stock ROM
                        notification = isNotificationActive(statusBarNotifications, "com.google.android.dialer", false/*, context*/);
                        if (notification != null) {
                            if (_duration != 0) {
                                long postTime = notification.getPostTime() + this._duration * 1000;

                                /*if (PPApplication.logEnabled()) {
                                    Calendar calendar = Calendar.getInstance();
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "current time=" + calendar.getTime());

                                    calendar.setTimeInMillis(postTime);
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "notification postTime=" + calendar.getTime());
                                }*/

                                if (System.currentTimeMillis() < postTime)
                                    return true;
                            } else
                                return true;
                        }
                        // Samsung, MIUI, EMUI, Sony
                        notification = isNotificationActive(statusBarNotifications, "android.incallui", true/*, context*/);
                        if (notification != null) {
                            if (_duration != 0) {
                                long postTime = notification.getPostTime() + this._duration * 1000;

                                /*if (PPApplication.logEnabled()) {
                                    Calendar calendar = Calendar.getInstance();
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "current time=" + calendar.getTime());

                                    calendar.setTimeInMillis(postTime);
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "notification postTime=" + calendar.getTime());
                                }*/

                                if (System.currentTimeMillis() < postTime)
                                    return true;
                            } else
                                return true;
                        }
                    }
                    if (this._missedCall) {
                        // Samsung, MIUI, Nexus/Pixel??? stock ROM, Sony
                        notification = isNotificationActive(statusBarNotifications, "com.android.server.telecom", false/*, context*/);
                        if (notification != null) {
                            if (_duration != 0) {
                                long postTime = notification.getPostTime() + this._duration * 1000;

                                /*if (PPApplication.logEnabled()) {
                                    Calendar calendar = Calendar.getInstance();
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "current time=" + calendar.getTime());

                                    calendar.setTimeInMillis(postTime);
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "notification postTime=" + calendar.getTime());
                                }*/

                                if (System.currentTimeMillis() < postTime)
                                    return true;
                            } else
                                return true;
                        }
                        // Samsung One UI
                        notification = isNotificationActive(statusBarNotifications, "com.samsung.android.dialer", false/*, context*/);
                        if (notification != null) {
                            if (_duration != 0) {
                                long postTime = notification.getPostTime() + this._duration * 1000;

                                /*if (PPApplication.logEnabled()) {
                                    Calendar calendar = Calendar.getInstance();
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "current time=" + calendar.getTime());

                                    calendar.setTimeInMillis(postTime);
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "notification postTime=" + calendar.getTime());
                                }*/

                                if (System.currentTimeMillis() < postTime)
                                    return true;
                            } else
                                return true;
                        }
                        // LG
                        notification = isNotificationActive(statusBarNotifications, "com.android.phone", false/*, context*/);
                        if (notification != null) {
                            if (_duration != 0) {
                                long postTime = notification.getPostTime() + this._duration * 1000;

                                /*if (PPApplication.logEnabled()) {
                                    Calendar calendar = Calendar.getInstance();
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "current time=" + calendar.getTime());

                                    calendar.setTimeInMillis(postTime);
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "notification postTime=" + calendar.getTime());
                                }*/

                                if (System.currentTimeMillis() < postTime)
                                    return true;
                            } else
                                return true;
                        }
                        // EMUI
                        notification = isNotificationActive(statusBarNotifications, "com.android.contacts", false/*, context*/);
                        if (notification != null) {
                            if (_duration != 0) {
                                long postTime = notification.getPostTime() + this._duration * 1000;

                                /*if (PPApplication.logEnabled()) {
                                    Calendar calendar = Calendar.getInstance();
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "current time=" + calendar.getTime());

                                    calendar.setTimeInMillis(postTime);
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "notification postTime=" + calendar.getTime());
                                }*/

                                if (System.currentTimeMillis() < postTime)
                                    return true;
                            } else
                                return true;
                        }
                    }

                    String[] splits = this._applications.split("\\|");
                    for (String split : splits) {
                        // get only package name = remove activity
                        String packageName = Application.getPackageName(split);
                        //PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "packageName=" + packageName);
                        // search for package name in saved package names
                        notification = isNotificationActive(statusBarNotifications, packageName, false/*, context*/);
                        //PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "notification=" + notification);
                        if (notification != null) {
                            if (_duration != 0) {
                                long postTime = notification.getPostTime() + this._duration * 1000;

                                /*if (PPApplication.logEnabled()) {
                                    Calendar calendar = Calendar.getInstance();
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "current time=" + calendar.getTime());

                                    calendar.setTimeInMillis(postTime);
                                    PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "notification postTime=" + calendar.getTime());
                                }*/

                                if (System.currentTimeMillis() < postTime)
                                    return true;
                            } else
                                return true;
                        }
                    }
                } catch (Exception e) {
                    Log.e("EventPreferencesNotification.isNotificationVisible", Log.getStackTraceString(e));
                    //FirebaseCrashlytics.getInstance().recordException(e);
                    Crashlytics.logException(e);
                }

                return false;
            }
        }

        /*
        // get all saved notifications
        PPNotificationListenerService.getNotifiedPackages(dataWrapper.context);

        // com.android.incallui - in call
        // com.samsung.android.incallui - in call
        // com.google.android.dialer - in call
        // com.android.server.telecom - missed call

        if (this._inCall) {
            // Nexus/Pixel??? stock ROM
            PostedNotificationData notification = PPNotificationListenerService.getNotificationPosted("com.google.android.dialer", false);
            if (notification != null)
                return true;
            // Samsung, MIUI, Sony
            notification = PPNotificationListenerService.getNotificationPosted("android.incallui", true);
            if (notification != null)
                return true;
        }
        if (this._missedCall) {
            // Samsung, MIUI, Nexus/Pixel??? stock ROM, Sony
            PostedNotificationData notification = PPNotificationListenerService.getNotificationPosted("com.android.server.telecom", false);
            if (notification != null)
                return true;
            // LG
            notification = PPNotificationListenerService.getNotificationPosted("com.android.phone", false);
            if (notification != null)
                return true;
        }

        String[] splits = this._applications.split("\\|");
        for (String split : splits) {
            // get only package name = remove activity
            String packageName = Application.getPackageName(split);
            // search for package name in saved package names
            PostedNotificationData notification = PPNotificationListenerService.getNotificationPosted(packageName, false);
            if (notification != null)
                return true;
        }*/

        return false;
    }

    private StatusBarNotification getNewestVisibleNotification(Context context) {
        //PPApplication.logE("EventPreferencesNotification.isNotificationVisible", "xxx");
        if (PPNotificationListenerService.isNotificationListenerServiceEnabled(context)) {
            PPNotificationListenerService service = PPNotificationListenerService.getInstance();
            if (service != null) {
                StatusBarNotification[] statusBarNotifications = service.getActiveNotifications();
                StatusBarNotification newestNotification = null;
                StatusBarNotification notification;

                if (this._inCall) {
                    // Nexus/Pixel??? stock ROM
                    notification = isNotificationActive(statusBarNotifications, "com.google.android.dialer", false/*, context*/);
                    if (notification != null) {
                        //noinspection ConstantConditions
                        if ((newestNotification == null) || (notification.getPostTime() > newestNotification.getPostTime()))
                            newestNotification = notification;
                    }
                    // Samsung, MIUI, EMUI, Sony
                    notification = isNotificationActive(statusBarNotifications, "android.incallui", true/*, context*/);
                    if (notification != null) {
                        if ((newestNotification == null) || (notification.getPostTime() > newestNotification.getPostTime()))
                            newestNotification = notification;
                    }
                }
                if (this._missedCall) {
                    // Samsung, MIUI, Nexus/Pixel??? stock ROM, Sony
                    notification = isNotificationActive(statusBarNotifications, "com.android.server.telecom", false/*, context*/);
                    if (notification != null) {
                        if ((newestNotification == null) || (notification.getPostTime() > newestNotification.getPostTime()))
                            newestNotification = notification;
                    }
                    // Samsung One UI
                    notification = isNotificationActive(statusBarNotifications, "com.samsung.android.dialer", false/*, context*/);
                    if (notification != null) {
                        if ((newestNotification == null) || (notification.getPostTime() > newestNotification.getPostTime()))
                            newestNotification = notification;
                    }
                    // LG
                    notification = isNotificationActive(statusBarNotifications, "com.android.phone", false/*, context*/);
                    if (notification != null) {
                        if ((newestNotification == null) || (notification.getPostTime() > newestNotification.getPostTime()))
                            newestNotification = notification;
                    }
                    // EMUI
                    notification = isNotificationActive(statusBarNotifications, "com.android.contacts", false/*, context*/);
                    if (notification != null) {
                        if ((newestNotification == null) || (notification.getPostTime() > newestNotification.getPostTime()))
                            newestNotification = notification;
                    }
                }

                String[] splits = this._applications.split("\\|");
                for (String split : splits) {
                    // get only package name = remove activity
                    String packageName = Application.getPackageName(split);
                    // search for package name in saved package names
                    notification = isNotificationActive(statusBarNotifications, packageName, false/*, context*/);
                    if (notification != null) {
                        if ((newestNotification == null) || (notification.getPostTime() > newestNotification.getPostTime()))
                            newestNotification = notification;
                    }
                }

                return newestNotification;
            }
        }

        return null;
    }

    private boolean isContactConfigured(String text/*, Context context*/) {
        //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "text=" + text);

        boolean phoneNumberFound = false;

        // find phone number in groups
        String[] splits = this._contactGroups.split("\\|");
        //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "_contactGroups.splits=" + splits.length);
        for (String split : splits) {
            /*String[] projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID};
            String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=? AND "
                    + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                    + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
            String[] selectionArgs = new String[]{split};
            Cursor mCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);
            //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "_contactGroups mCursor=" + mCursor);
            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    String contactId = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                    String[] projection2 = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                    String selection2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
                    String[] selection2Args = new String[]{contactId};
                    Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection2, selection2, selection2Args, null);
                    //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "_contactGroups phones=" + phones);
                    if (phones != null) {
                        while (phones.moveToNext()) {
                            String _contactName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "_contactGroups _contactName=" + _contactName);

                            if (text.toLowerCase().contains(_contactName.toLowerCase())) {
                                phoneNumberFound = true;
                                break;
                            }
                        }
                        phones.close();
                    }
                    if (phoneNumberFound)
                        break;
                }
                mCursor.close();
            }*/

            if (!split.isEmpty()) {
                ContactsCache contactsCache = PhoneProfilesService.getContactsCache();
                if (contactsCache == null)
                    return false;

                synchronized (PPApplication.contactsCacheMutex) {
                    List<Contact> contactList = contactsCache.getList(true);
                    if (contactList != null) {
                        for (Contact contact : contactList) {
                            if (contact.groups != null) {
                                long groupId = contact.groups.indexOf(Long.valueOf(split));
                                if (groupId != -1) {
                                    // group found in contact
                                    String _contactName = contact.name;
                                    if (text.toLowerCase().contains(_contactName.toLowerCase())) {
                                        phoneNumberFound = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (phoneNumberFound)
                break;
        }
        //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "_contactGroups phoneNumberFound=" + phoneNumberFound);

        if (!phoneNumberFound) {
            // find phone number in contacts
            splits = this._contacts.split("\\|");
            //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "_contacts.splits=" + splits.length);
            for (String split : splits) {
                String[] splits2 = split.split("#");

                /*// get phone number from contacts
                String[] projection = new String[]{ContactsContract.Contacts._ID};
                String selection = ContactsContract.Contacts._ID + "=?";
                String[] selectionArgs = new String[]{splits2[0]};
                //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "_contacts splits2[0]=" + splits2[0]);
                Cursor mCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, selectionArgs, null);
                //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "_contacts mCursor=" + mCursor);
                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        String[] projection2 = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                        String selection2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
                        String[] selection2Args = new String[]{splits2[0]};
                        //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "_contacts splits2[0]=" + splits2[0]);
                        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection2, selection2, selection2Args, null);
                        //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "_contacts phones=" + phones);
                        if (phones != null) {
                            while (phones.moveToNext()) {
                                String _contactName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "_contacts _contactName=" + _contactName);

                                if (text.toLowerCase().contains(_contactName.toLowerCase())) {
                                    phoneNumberFound = true;
                                    break;
                                }
                            }
                            phones.close();
                        }
                        if (phoneNumberFound)
                            break;
                    }
                    mCursor.close();
                }*/

                if ((!split.isEmpty()) && (!splits2[0].isEmpty()) && (!splits2[1].isEmpty())) {
                    ContactsCache contactsCache = PhoneProfilesService.getContactsCache();
                    if (contactsCache == null)
                        return false;

                    synchronized (PPApplication.contactsCacheMutex) {
                        List<Contact> contactList = contactsCache.getList(false);
                        if (contactList != null) {
                            for (Contact contact : contactList) {
                                if ((contact.contactId == Long.valueOf(splits2[0])) && contact.phoneId == Long.valueOf(splits2[1])) {
                                    String _contactName = contact.name;
                                    if (text.toLowerCase().contains(_contactName.toLowerCase())) {
                                        phoneNumberFound = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if (phoneNumberFound)
                    break;
            }
        }
        //PPApplication.logE("EventPreferencesNotification.isContactConfigured", "_contacts phoneNumberFound=" + phoneNumberFound);

        return phoneNumberFound;
    }

}
