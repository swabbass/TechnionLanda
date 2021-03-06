package utils;

import android.content.Context;
import android.content.Intent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

import ward.landaMaan.Course;
import ward.landaMaan.R;
import ward.landaMaan.Teacher;
import ward.landaMaan.activities.Settings;

public class GCMUtils {

    private static final String PROPERTY_REG_ID;

    static {
        PROPERTY_REG_ID = "registration_id";
    }

    public static final String DATA = "data";
  //  public static final String SENDER_ID = "498258787681";//for development
    public static final String SENDER_ID = "479204617677"; //for deploy
    public static final String REGSITER = "isReg";
    public static final String LOAD_TEACHERS = "load_teachers";
    public static final String LOAD_UPDATES = "load_updates";
    public static final String LOAD_COURSES = "load_courses";
    public static final String REG_KEY = "REGKEY";
  // private static final String URL = "http://wabbass.byethost9.com/wordpress/";
    private static final String URL = "http://glanda.technion.ac.il/wordpress";
    public static final String TAG = "wordpress";
    private static final String NLANDA_GCM_REG = "http://nlanda.technion.ac.il/LandaSystem/registerGcm.aspx";

    public static String sendRegistrationIdToBackend(String regKey) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(URL + "/?regId=" + regKey);
        HttpPost httppostNlanda = new HttpPost(NLANDA_GCM_REG + "?reg_id="
                + regKey);
        try {
            HttpResponse response = httpclient.execute(httppost);
            HttpResponse resNlanda = httpclient.execute(httppostNlanda);
            return "";

        } catch (IOException e) {
        }

        return null;
    }

    public static Teacher HandleInstructor(String action, Context cxt,
                                           DBManager dbmngr, Intent intent)

    {
        Settings.initlizeSettings(cxt);
        Teacher tmp = new Teacher(intent.getStringExtra("fname"),
                intent.getStringExtra("lname"),
                intent.getStringExtra("email"),
                intent.getStringExtra("id"), "T",
                intent.getStringExtra("faculty"));
        if (action.equals("INSTRUCTOR")) {
            if (dbmngr.getTeacherByIdNumber(tmp.getId_number()) == null) {
                tmp.setDownloadedImage(false);
                dbmngr.insertTeacher(tmp);
                if (Settings.isToNotifyUpdates())
                    Utilities.showNotification(cxt, cxt.getResources().getString(R.string.TeacherAdded), tmp.getName()
                            + " " + tmp.getLast_name());

            } else {

                dbmngr.updateTeacher(tmp);
                if (Settings.isToNotifyUpdates())
                    Utilities.showNotification(cxt, cxt.getResources().getString(R.string.TeacherUpdated),
                            tmp.getName() + " " + tmp.getLast_name());
            }

        } else if (action.equals("RINSTRUCTOR")) {

            dbmngr.deleteTeacher(tmp);
            Utilities.showNotification(cxt, cxt.getResources().getString(R.string.TeacherDeleted), tmp.getName() + " " + tmp.getLast_name());
        }
        return tmp;
    }

    public static Course HandleWorkshop(String action, Context cxt,
                                        DBManager dbmngr, Intent intent) {
        Settings.initlizeSettings(cxt);
        Course cTmp = new Course(intent.getStringExtra("subject_name")
                , intent.getStringExtra("day"),
                intent.getStringExtra("time_from"),
                intent.getStringExtra("time_to"),
                intent.getStringExtra("place"));
        cTmp.setCourseID(Integer.parseInt(intent.getStringExtra("id")));
        cTmp.setTutor_id(intent.getStringExtra("tutor_id"));
        String subject_id = intent.getStringExtra("subject_id");

        if (action.equals("WORKSHOP")) {
            if (dbmngr.getCourseById(Integer.toString(cTmp.getCourseID())) == null) {
                cTmp.setSubject_id(subject_id);
                dbmngr.insertCourse(cTmp);
                if (Settings.isToNotifyUpdates())
                    Utilities.showNotification(cxt, cxt.getResources().getString(R.string.CourseAdded),
                            cTmp.getName() + " ");
            } else {
                cTmp.setSubject_id(subject_id);
                dbmngr.UpdateCourse(cTmp);
                if (Settings.isToNotifyUpdates())
                    Utilities.showNotification(cxt, cxt.getResources().getString(R.string.CourseUpdated),
                            cTmp.getName() + " ");
            }
        } else if (action.equals("RWORKSHOP")) {
            if (dbmngr.deleteCourse(cTmp)) {
                if (Settings.isToNotifyUpdates())
                    Utilities.showNotification(cxt, cxt.getResources().getString(R.string.CourseDeleted),
                            cTmp.getName() + " ");
            }
        }
        return cTmp;
    }

}
