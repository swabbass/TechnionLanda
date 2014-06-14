package ward.landa.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ward.landa.R;

public class Settings {

    public static final String SETTINGS = "SHAREDPREFRENCES";
    public static final int COURSES = 0;
    public static final int UPDATES = 2;
    public static final int TEACHERS = 1;
    public static final String HEBREW = "he";
    public static final String ENGLISH = "en";
    public static final String ARABIC = "ar";
    public static final String URL_UPDATES = "http://wabbass.byethost9.com/wordpress/?json=get_posts&count=15";
    public static final String URL_teachers = "http://nlanda.technion.ac.il/LandaSystem/tutors.aspx";
    public static final String URL_COURSES = "http://nlanda.technion.ac.il/LandaSystem/courses.aspx";
    public final static String picsPathDir = "Landa" + File.separator
            + "tutors" + File.separator;
    public final static String picFromRoot = Environment
            .getExternalStorageDirectory() + File.separator + picsPathDir;
    public static final String picFromAbsoulotePath = Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + picsPathDir;
    public static final String WARD_LANDA_ALARM = "ward.landa.ALARM";
    public static final String DISPLAY_MESSAGE_ACTION = "ward.landa.DISPLAY_MESSAGE";
    // public static final String ADD_CONVERSATION_ACTION =
    // "ward.landa.ADD_CONVERSATION";
    // public static final String DISPLAY_CONVERSATION_ACTION =
    // "ward.landa.DISPLAY_CONVERSATION";
    public static final String DISMISS_NOTIFICATION_ACTION = "ward.landa.DISMISS_NOTIFICATION";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_Date = "date";
    public static final String EXTRA_TITLE = "title";
    private static final String TO_NOTIFY_UPDATE = "toNotifyUpdate";
    private static final String TO_CORSE_NOTIFY = "toCorseNotify";
    private static final String LOCAL_KEY = "local";
    public static SimpleDateFormat sDf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm");
    private static String localLang;
    private static boolean toNotifyUpdates;
    private static boolean toNotifyCourse;

    public static void initlizeSettings(Context c) {
        SharedPreferences settings = c.getSharedPreferences(SETTINGS,
                Activity.MODE_PRIVATE);
        localLang = settings.getString(LOCAL_KEY, HEBREW);
        toNotifyCourse = settings.getBoolean(TO_CORSE_NOTIFY, true);
        toNotifyUpdates = settings.getBoolean(TO_NOTIFY_UPDATE, true);
    }

    public static String getexactTime() {

        Date cal = Calendar.getInstance().getTime();
        return sDf.format(cal);
    }

    public static void saveSettings(Context c, String local,
                                    boolean courseNotify, boolean updateNotify) {
        SharedPreferences settings = c.getSharedPreferences(SETTINGS,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(TO_CORSE_NOTIFY, courseNotify);
        editor.putBoolean(TO_NOTIFY_UPDATE, updateNotify);
        editor.putString(LOCAL_KEY, local);
        editor.commit();
    }

    public static String getLocalLang() {
        return localLang;
    }

    public static void setLocalLang(String localLang) {
        Settings.localLang = localLang;
    }

    public static boolean isToNotifyUpdates() {
        return toNotifyUpdates;
    }

    public static void setToNotifyUpdates(boolean toNotifyUpdates) {
        Settings.toNotifyUpdates = toNotifyUpdates;
    }

    public static boolean isToNotifyCourse() {
        return toNotifyCourse;
    }

    public static void setToNotifyCourse(boolean toNotifyCourse) {
        Settings.toNotifyCourse = toNotifyCourse;
    }

    public static int langId(String lang) {
        if (lang != null && !lang.isEmpty()) {
            switch (lang) {
                case HEBREW:
                    return R.id.radioHE;
                case ARABIC:
                    return R.id.radioAr;
            }
        }
        return -1;
    }
}
