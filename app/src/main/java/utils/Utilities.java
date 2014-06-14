package utils;

import android.app.Activity;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import ward.landa.R;
import ward.landa.Update;
import ward.landa.activities.MainActivity;
import ward.landa.activities.Settings;

public class Utilities {

    public static final String NEW_UPDATE = "new_Update";
    private static final int notfyId = 12;
    public static HashMap<String, String> files;

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
     * removes all kind of tags like <13455> will be removed
     *  -perfomance o(n) ..n itirations
     * @param inp html text input
     * @return
     */
    public static String html2Text(String inp) {
        boolean intag = false;
        String outp = "";

        for (int i = 0; i < inp.length(); ++i) {

            char c = inp.charAt(i);
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
        return outp;
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
        for (int i = 0; i < tables.size(); ++i) {

            Table t = tables.get(i);
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
        int tableIndex = -1;
        int count = 0;
        List<Table> tables = new ArrayList<Utilities.Table>();
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

    public static InputMethodManager getInputMethodManager(Activity activity) {
        return (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /**
     * @param outBuffer String buffer of the text to replace % and escaped characters
     * @return Formated String with replaced tags and encodded
     */
    public static String replacer(StringBuffer outBuffer) {

        String data = outBuffer.toString();
        try {
            StringBuffer tempBuffer = new StringBuffer();
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
     * @param context Context
     * @param message Message to send to BoroadCastReciever
     * @param time    Time day hour minute seconds milliseconds
     * @param title   Title of the message
     * @param Action  Action type for intent
     */
    public static void displayMessage(Context context, String message,
                                      String time, String title, String Action) {
        Intent intent = new Intent(Action);
        intent.putExtra(Settings.EXTRA_MESSAGE, message);
        intent.putExtra(Settings.EXTRA_Date, time);
        intent.putExtra(Settings.EXTRA_TITLE, title);
        context.sendBroadcast(intent);
    }

    /**
     * @param context Context
     * @param update  Update object to send to broadcasrreciever
     * @param Action  ActionType for intent
     */
    public static void displayMessageUpdate(Context context, Update update,
                                            String Action) {
        Intent intent = new Intent(Action);
        intent.putExtra(NEW_UPDATE, update);
        context.sendBroadcast(intent);
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
                    file.createNewFile();
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
    public static Update generateUpdateFromExtras(Bundle extras, Context cxt) {
        String msg = extras.getString(Settings.EXTRA_MESSAGE);
        String subject = extras.getString("title");
        Update u = null;
        String[] data = msg.split("\n");
        String post = data[0];
        if (!post.equals("Post updated")) {

            try {
                JSONObject jObj = new JSONObject(msg);
                String id = jObj.getString("ID");
                String title = subject;
                String content = Utilities.html2Text(jObj.getString("post_content"));
                String date = jObj.getString("post_date");
                String url = jObj.getString("guid");
                u = new Update(id, title, date, content, false);
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
        public int endIndex;
        public int firstTrStartIndex;
        public int firstTrEndIndex;

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
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        boolean downloadOk = false;
        Context cxt;
        ConnectionDetector connectionDetector;
        Update u;
        PostListener listner;
        String url = "http://wabbass.byethost9.com/wordpress/";

        public fetchUpdateFromBackEndTask(Context cxt, PostListener listner) {
            this.cxt = cxt;
            this.listner = listner;
            this.u = null;
            connectionDetector = new ConnectionDetector(cxt);
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
                                Utilities.html2Text(update.getString("content")), false);
                        u.setUrl(update.getString("url"));

                        downloadOk = true;
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

}
