package utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import ward.landa.Course;
import ward.landa.R;
import ward.landa.Update;
import ward.landa.activities.MainActivity;
import ward.landa.activities.Reciever;
import ward.landa.activities.Settings;

public class Utilities {

    private static final String NEW_UPDATE = "new_Update";
    private static final int notfyId = 12;

    /**
     * Saving the download data once per key in the Shared Perfrences
     *
     * @param value the vlaue
     * @param key   the key GCMutils key
     * @param cxt   Context
     */
    public static void saveDownloadOnceStatus(boolean value, final String key,
                                              Context cxt) {
        SharedPreferences sh = cxt.getSharedPreferences(GCMUtils.DATA,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor ed = sh.edit();
        ed.putBoolean(key, value);
        ed.commit();

    }

    /**
     *  when new semester starts reset all the courses and teachers
     * @param context Context
     */
    public static void resetForNewSemester(Context context,DBManager dbManager)
    {
        saveDownloadOnceStatus(false,GCMUtils.LOAD_COURSES,context);
        saveDownloadOnceStatus(false,GCMUtils.LOAD_TEACHERS,context);
        dbManager.resetSemester();
    }

    /**
     * removes all kind of tags like <13455> will be removed
     *  -perfomance o(n) ..n itirations
     * @param inp html text input
     * @return String fromtated without any html tag
     */
    public static String html2Text(String inp) {
        boolean intag = false;
        String outp = "";
        DBManager.removeQoutes(inp);
        for (int i = 0; i < inp.length(); ++i) {

            if (!intag && inp.charAt(i) == '<') {
                intag = true;
                continue;
            }
            if (intag && inp.charAt(i) != '>') {

                continue;
            }
            if (intag && inp.charAt(i) == '>') {
                intag = false;
                continue;
            }
            if (!intag) {
                outp = outp + inp.charAt(i);
            }
        }
        return Jsoup.parse(outp).text();
    }

    /**
     * Removing first row in the table tag (names of columns)
     *
     * @param html html to parse
     * @return html text without table if they were existed ,else the same text
     */
    public static String removeTableFirstTrHtml(String html) {
        String tmp = html;
        List<Table> tables = getTableTags(tmp);
        for (Table t : tables) {

            int firstTrTag = html.indexOf("<tr", t.startIndex);
            int lastTrTag = html.indexOf("</tr>", t.startIndex)
                    + "</tr>".length();
            String firstTr = null;
            if (firstTrTag != -1 && lastTrTag != -1)
                firstTr = html.substring(firstTrTag, lastTrTag);
            if (firstTr != null) {
                String empty = "";
                for (int j = 0; j < firstTr.length(); j++) {
                    empty += " ";
                }
                html = html.replace(firstTr, empty);
            }
        }
        return html;
    }

    /**
     * getting list of tables in html text with Table objects
     *
     * @param html html text to fetch data from
     * @return list of tables in the html
     */
    private static List<Table> getTableTags(String html) {
        int tableIndex;
        int count = 0;
        List<Table> tables = new ArrayList<>();
        while ((tableIndex = html.indexOf(Table.TABLE_OPENER)) != -1) {
            int tableCloserIndex = html.indexOf(Table.TABLE_CLOSER)
                    + Table.TABLE_CLOSER.length();
            if (tableCloserIndex != -1) {
                count++;
                Table t = new Table();
                t.startIndex = tableIndex;
                t.endIndex = tableCloserIndex;
                String empty = "";
                for (int i = 0; i < tableCloserIndex - tableIndex; i++) {
                    empty += " ";
                }
                t.firstTrStartIndex = html.indexOf(Table.TR_OPENER, tableIndex);
                t.firstTrEndIndex = html.indexOf(Table.TR_CLOSER, tableIndex);
                String str = html.substring(tableIndex, tableCloserIndex);
                html = html.replace(str, empty);
                tables.add(t);
            }
        }
        return tables;
    }

    /**
     * @param outBuffer String buffer of the text to replace % and escaped characters
     * @return Formated String with replaced tags and encodded
     */
    public static String replacer(StringBuffer outBuffer) {

        String data = outBuffer.toString();
        try {
            StringBuilder tempBuffer = new StringBuilder();
            int incrementor = 0;
            int dataLength = data.length();
            while (incrementor < dataLength) {
                char charecterAt = data.charAt(incrementor);
                if (charecterAt == '%') {
                    tempBuffer.append("<percentage>");
                } else if (charecterAt == '+') {
                    tempBuffer.append("<plus>");
                } else {
                    tempBuffer.append(charecterAt);
                }
                incrementor++;
            }
            data = tempBuffer.toString();
            data = URLDecoder.decode(data, "UTF-8");
            data = data.replaceAll("<percentage>", "%");
            data = data.replaceAll("<plus>", "+");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * @param hebrewDay day in hebrew language
     * @return Calendar.SUNDAY- Calendar.THURSDAY
     */
    public static int dayWeekNumber(String hebrewDay) {

        switch (hebrewDay.replaceAll("\\s", "")) {
            case "ראשון":
                return Calendar.SUNDAY;
            case "שני":
                return Calendar.MONDAY;
            case "שלישי":
                return Calendar.TUESDAY;
            case "רבעי":
                return Calendar.WEDNESDAY;
            case "חמישי":
                return Calendar.THURSDAY;

        }
        return -1;
    }

    /**
     * Checks if the given path to file is existed !
     *
     * @param localPath Path to file
     * @return true existed ,otherwise false
     */
    public static boolean checkIfImageExistsInSd(String localPath) {

        File f = new File(localPath);
        return f.exists();

    }

    /**
     * @param localPath : image path is the id with suffix .jpg
     * @param bmp       : bitmap from the cache to save
     * @return the path to the pic
     */
    public static String saveImageToSD(String localPath, Bitmap bmp) {
        FileOutputStream fos = null;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        // check external state
        String dirPath = Environment.getExternalStorageDirectory()
                + File.separator + Settings.picsPathDir;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }

        File file = new File(localPath);
        try {
            if (!file.createNewFile()) {
                if (file.delete()) {
                   if( file.createNewFile())
                   {

                   }
                } else {
                    throw new IOException("y3nn roma ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            assert fos != null;
            fos.write(bytes.toByteArray());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("eee", "saved Image : " + localPath);
        return localPath;

    }

    /**
     * @param extras from intent
     * @return new Update with the message delivered from back-end
     */
    public static Update generateUpdateFromExtras(Bundle extras) {
        String msg = extras.getString(Settings.EXTRA_MESSAGE);

        Update u = null;
        String[] data = msg.split("\n");
        String post = data[0];
        if (!post.equals("Post updated")) {

            try {
                JSONObject jObj = new JSONObject(msg);
                String id = jObj.getString("ID");
                String title = extras.getString("title");
                String content = Utilities.html2Text(jObj.getString("post_content"));
                content=content.replaceAll("&#8221;",Pattern.quote("\""));
                String date = jObj.getString("post_date");
                String url = jObj.getString("guid");
                u = new Update(id, title, date, content, jObj.getString("post_content"));
                u.setUrl(url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // Update FromBackGroudn:
            // http://wabbass.byethost9.com/wordpress/?p=34
            String[] postInfo = data[1].split(":");// title : url
            String url = postInfo[1] + ":" + postInfo[2];

            String idTemp = postInfo[2].split(Pattern.quote("?"))[1];// p=34
            url += "&json=1";
            String id = idTemp.split("=")[1];// 34
            u = new Update(id, url);

        }
        return u;
    }

    /**
     * Shows notification on the notification bar
     *
     * @param context Context
     * @param title   Notification title
     * @param text    Notification text content (info)
     */
    public static void showNotification(Context context, String title,
                                        String text) {

        // Notification notification=new Notification(R.drawable.success,
        // tickerText, when)
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true).setContentTitle(title)
                .setContentText(text).setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        // PendingMsgs.add(msg);
        Intent i = new Intent(context, MainActivity.class);
        // inorder to return to home if back pressed
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(i);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        nBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notfyId, nBuilder.build());
    }

    /**
     * opens google maps to navigate with directions to destination
     * @param source source cordinates x,y
     * @param dest  destination cordinates x.y
     * @param cxt   Context to open intent for constants usage
     */
    public static void openMapToNavigate(String source, String dest, Context cxt) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://ditu.google.cn/maps?f=d&source=s_d" +
                        "&saddr=" + source + "&daddr=" + dest + "&hl=zh&t=m&dirflg=w")
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        cxt.startActivity(intent);
    }

    /**
     *  open the map on exact location without navigation
     * @param latitude x cordinate
     * @param longitude y cordinage
     * @param cxt Context
     */
    public static void openMapInLocation(double latitude,double longitude,Context cxt)
    {
        String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        cxt.startActivity(intent);
    }

    /**
     * Gets the location (x,y) formate for given address in technion
     * @param address one of strings for locations in Values
     * @param res resourses to get strings
     * @return the x,y formated string with cordinates
     */
    public static String getLocationByAddress(String address, Resources res) {


        if (address.contains(res.getString(R.string.ulman))) {
            return "32.777039, 35.023238";
        }
        if (address.contains(res.getString(R.string.rabin)))

        {
            return "32.778789, 35.022141";
        }
        if (address.contains(res.getString(R.string.taub))) {
            return "32.777658, 35.021373";
        }
        if (address.contains(res.getString(R.string.sego))) {
            return "32.777806, 35.022337";
        }
        if (address.contains(res.getString(R.string.feshbach))) {
            return "32.776369, 35.024051";
        }
        if (address.contains(res.getString(R.string.mayer))) {
            return "32.775589, 35.024882";
        }
        if (address.contains(res.getString(R.string.chemya))) {
            return "32.778155, 35.027575";
        }

        return null;


    }


    /**
     * after getting Update for an existed update call back
     *
     * @author wabbass
     */
    public static interface PostListener {
        public void onPostUpdateDownloaded(Update u);

    }

    static class Table {
        public static final String TABLE_OPENER = "<table";
        public static final String TABLE_CLOSER = "</table>";
        public static final String TR_OPENER = "<tr";
        public static final String TR_CLOSER = "</tr>";
        /**
         * start index of the table tag start
         */
        public int startIndex;
        /**
         * the last index of the close table tag
         */
        private int endIndex;
        private int firstTrStartIndex;
        private int firstTrEndIndex;

        public Table() {
            // TODO Auto-generated constructor stub
        }
    }

    /**
     * AsyncTask to Fetch Update from the internet by post id given from the
     * push notification
     *
     * @author wabbass
     */
    public static class fetchUpdateFromBackEndTask extends
            AsyncTask<String, String, Update> {
       final List<NameValuePair> params = new ArrayList<>();
        final WeakReference<Context> cxt;
       final ConnectionDetector connectionDetector;
        Update u;
      final  PostListener listner;
       final String url = "http://wabbass.byethost9.com/wordpress/";

        public fetchUpdateFromBackEndTask(Context cxt, PostListener listner) {
            this.cxt = new WeakReference<Context>(cxt);
            this.listner = listner;
            this.u = null;
            connectionDetector = new ConnectionDetector(this.cxt.get());
        }

        @Override
        protected Update doInBackground(String... params) {
            JSONParser jParser = new JSONParser();
            if (params[0] != null) {
                this.params.add(new BasicNameValuePair("p", params[0]));
                this.params.add(new BasicNameValuePair("json", "1"));
                JSONObject jObject = jParser.makeHttpRequest(url, "GET",
                        this.params);
                if (jObject == null) {
                    if (cancel(true)) {
                        Log.e(GCMUtils.TAG,
                                "loading Updates from internet canceled");
                    }
                } else {
                    Log.d("ward", jObject.toString());
                    try {
                        JSONObject update = jObject.getJSONObject("post");
                        Log.d("ward", jObject.toString());

                        u = new Update(update.getString("id"),
                                update.getString("title"),
                                update.getString("date"),
                                Utilities.html2Text(update.getString("content")), update.getString("content"));
                        u.setUrl(update.getString("url"));

                    } catch (JSONException e) {

                        e.printStackTrace();
                        Log.e(GCMUtils.TAG, e.toString());
                        if (!connectionDetector.isConnectingToInternet()) {
                            Log.e(GCMUtils.TAG, "faild no internet ");
                            cancel(true);
                        }

                    }
                }

            }
            return u;
        }

        @Override
        protected void onPostExecute(Update result) {

            listner.onPostUpdateDownloaded(result);
            super.onPostExecute(result);
        }

    }

    /**
     *  After device reboot register the alarams for notified courses
     * @param courses List of notified Courses
     * @param context   Context
     */
    public   static void resetAlarnsAfterReboot(List<Course> courses,Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for(Course c:courses)
        {
            int dayNum = Utilities.dayWeekNumber(c.getDateTime());
            if (dayNum == -1)
                return;
            int hourFrom = Integer.parseInt(c.getTimeFrom().split(":")[0]
                    .replaceAll("\\s", ""));
            int minFrom = Integer.parseInt(c.getTimeFrom().split(":")[1]
                    .replaceAll("\\s", ""));
            if (minFrom - 20 < 0) {
                hourFrom--;
                minFrom = 40;
            }
                minFrom -= 20;
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, dayNum);
                calendar.set(Calendar.HOUR_OF_DAY, hourFrom);
                calendar.set(Calendar.MINUTE, minFrom);
                calendar.set(Calendar.SECOND, 0);
                Intent myIntent = new Intent(context, Reciever.class);
                myIntent.setAction(Settings.WARD_LANDA_ALARM);
                myIntent.putExtra("course", c);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                        c.getCourseID(), myIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent);
            calendar.set(Calendar.MINUTE, minFrom + 10);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent);

        }
    }
}
