package ward.landaMaan.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.Html;

import java.util.List;
import java.util.Set;
import utils.DBManager;
import utils.GCMUtils;
import utils.Utilities;
import utils.Utilities.PostListener;
import ward.landaMaan.Course;
import ward.landaMaan.R;
import ward.landaMaan.Update;

public class Reciever extends BroadcastReceiver implements PostListener {
    private DBManager dbmngr;
    private Context cxt;

    @Override
    public void onReceive(Context context, Intent intent) {

        dbmngr = new DBManager(context);
        Set<String> keys = intent.getExtras().keySet();
        this.cxt = context;
        Settings.initlizeSettings(context);

        if (intent.getAction()
                .compareTo("com.google.android.c2dm.intent.RECEIVE") == 0) {

            if (intent.getStringExtra("Type") != null) {
                String t = intent.getStringExtra("Type");
                if (t.contains("INSTRUCTOR")) {
                    GCMUtils.HandleInstructor(t, context, dbmngr, intent);
                } else if (t.contains("WORKSHOP")) {
                    GCMUtils.HandleWorkshop(t, context, dbmngr, intent);
                }
            } else {
                Update u = Utilities.generateUpdateFromExtras(
                        intent.getExtras());
                if (u != null && u.getUrlToJason() == null) {
                    dbmngr.insertUpdate(u);
                    if (Settings.isToNotifyUpdates())
                        Utilities.showNotification(context, u.getSubject(),
                                Utilities.html2Text(u.getText()));
                } else {
                    Utilities.fetchUpdateFromBackEndTask task = new Utilities.fetchUpdateFromBackEndTask(
                            context, this);
                    assert u != null;
                    task.execute(u.getUpdate_id());
                }
            }
        }
        if (intent.getAction()
                .compareTo("android.intent.action.BOOT_COMPLETED") == 0) {

            List<Course> notifiedCourses=dbmngr.getAllNotifiedCourses();

                Utilities.resetAlarnsAfterReboot(notifiedCourses,context);
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
