package ward.landaMaan.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;

import ward.landaMaan.R;

public class Settings {

    private static final String SETTINGS;

    static {
        SETTINGS = "SHAREDPREFRENCES";
    }

    public static final int COURSES = 0;
    public static final int TEACHERS = 1;
    public static final String HEBREW = "he";
    public static final String ENGLISH = "en";
    public static final String ARABIC = "ar";
   // public static final String URL_UPDATES = "http://wabbass.byethost9.com/wordpress/?json=get_posts&count=15"; //development
    public static final String URL_UPDATES = "http://glanda.technion.ac.il/wordpress/?json=get_posts&count=15"; //for deploy
    public static final String URL_teachers = "http://nlanda.technion.ac.il/LandaSystem/tutors.aspx";
    public static final String URL_COURSES = "http://nlanda.technion.ac.il/LandaSystem/courses.aspx";
    public final static String picsPathDir = "Landa" + File.separator
            + "tutors" + File.separator;
    public static final String picFromAbsoulotePath = Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + picsPathDir;
    public static final String WARD_LANDA_ALARM = "ward.landa.ALARM";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_Date = "date";
    public static final String EXTRA_TITLE = "title";
    private static final String TO_NOTIFY_UPDATE = "toNotifyUpdate";
    private static final String TO_CORSE_NOTIFY = "toCorseNotify";
    private static final String RICH_VIEW = "rich_view";
    private static final String LOCAL_KEY = "local";
    private static final SimpleDateFormat sDf;

    static {
        sDf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm");
    }

    private static String localLang;
    private static boolean toNotifyUpdates;
    private static boolean toNotifyCourse;
    private  static boolean richView;

    public static void initlizeSettings(Context c) {
        SharedPreferences settings = c.getSharedPreferences(SETTINGS,
                Activity.MODE_PRIVATE);
        localLang = settings.getString(LOCAL_KEY, HEBREW);
        toNotifyCourse = settings.getBoolean(TO_CORSE_NOTIFY, true);
        toNotifyUpdates = settings.getBoolean(TO_NOTIFY_UPDATE, true);
        richView=settings.getBoolean(RICH_VIEW, false);
    }

    public static void saveSettings(Context c, String local,
                                    boolean courseNotify, boolean updateNotify,boolean isRichView) {
        SharedPreferences settings = c.getSharedPreferences(SETTINGS,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(TO_CORSE_NOTIFY, courseNotify);
        editor.putBoolean(TO_NOTIFY_UPDATE, updateNotify);
        editor.putBoolean(RICH_VIEW, isRichView);
        editor.putString(LOCAL_KEY, local);
        editor.commit();
    }

    public  static void resetSettings(Context context)
    {
        saveSettings(context,HEBREW,true,true,true);
    }
    public static String getLocalLang() {
        return localLang;
    }

    public static boolean isToNotifyUpdates() {
        return toNotifyUpdates;
    }

    public static boolean isToNotifyCourse() {
        return toNotifyCourse;
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

    public static boolean isRichView() {
        return richView;
    }

}
