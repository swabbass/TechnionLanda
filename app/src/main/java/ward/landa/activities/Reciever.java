package ward.landa.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;

import java.util.Set;

import utils.DBManager;
import utils.GCMUtils;
import utils.Utilities;
import utils.Utilities.PostListener;
import ward.landa.Course;
import ward.landa.R;
import ward.landa.Update;

public class Reciever extends BroadcastReceiver implements PostListener {
    final public static String ONE_TIME = "onetime";
    DBManager dbmngr;
    Context cxt;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("wordpress", "" + intent.getAction());
        Set<String> keys = intent.getExtras().keySet();
        this.cxt = context;
        Settings.initlizeSettings(context);
        for (String key : keys) {
            Log.d("wordpress", key + " : " + intent.getExtras().getString(key));
        }
        if (intent.getAction().toString()
                .compareTo("com.google.android.c2dm.intent.RECEIVE") == 0) {
            dbmngr = new DBManager(context);
            if (intent.getStringExtra("Type") != null) {
                String t = intent.getStringExtra("Type");
                if (t.contains("INSTRUCTOR")) {
                    GCMUtils.HandleInstructor(t, context, dbmngr, intent);
                } else if (t.contains("WORKSHOP")) {
                    GCMUtils.HandleWorkshop(t, context, dbmngr, intent);
                }
            } else {
                Update u = Utilities.generateUpdateFromExtras(
                        intent.getExtras(), context);
                if (u != null && u.getUrlToJason() == null) {
                    dbmngr.insertUpdate(u);
                    if (Settings.isToNotifyUpdates())
                        Utilities.showNotification(context, u.getSubject(),
                                Utilities.html2Text(u.getText()));
                } else {
                    Utilities.fetchUpdateFromBackEndTask task = new Utilities.fetchUpdateFromBackEndTask(
                            context, this);
                    task.execute(u.getUpdate_id());
                }
            }
        }
        if (intent.getAction().equals(Settings.WARD_LANDA_ALARM)) {
            Course c = (Course) intent.getSerializableExtra("course");
            String s = String.format("%s : %s \n  %s : %s", cxt.getResources()
                    .getString(R.string.Place), c.getPlace(), cxt
                    .getResources().getString(R.string.Time), c.getTimeFrom());
            if (Settings.isToNotifyCourse())
                Utilities.showNotification(context, cxt.getResources()
                                .getString(R.string.WorkshopAlarm) + " " + c.getName(),
                        s
                );
        }

    }

    @Override
    public void onPostUpdateDownloaded(Update u) {
        if (u != null && cxt != null) {
            dbmngr.updateUpdate(u);
            Settings.initlizeSettings(cxt);
            if (Settings.isToNotifyUpdates())
                Utilities.showNotification(cxt, u.getSubject(),
                        Html.fromHtml(u.getText()).toString());
        }

    }

}
