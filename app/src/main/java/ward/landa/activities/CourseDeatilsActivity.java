package ward.landa.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import utils.DBManager;
import ward.landa.Course;
import ward.landa.CourseNotification;
import ward.landa.R;
import ward.landa.fragments.CourseFragment;

public class CourseDeatilsActivity extends FragmentActivity implements
        CourseFragment.AlarmCallBack {

    Intent result;
    DBManager dbManager;
    private String courseName;
    private int imgId;
    private int courseID;
    private int parentIndex = -1;
    private List<CourseDetailHolder> checkedCourses;

    private CourseNotification courseNotification;

    private void fetchArguments() {
        Bundle ex = getIntent().getExtras();
        if (ex != null) {
            this.setCourseName(ex.getString("name"));
            this.setImgId(ex.getInt("ImageID"));
            this.setCourseID(ex.getInt("courseID"));
            this.setParentIndex(ex.getInt("position"));
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(
                    View.LAYOUT_DIRECTION_RTL);

        } else {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceRTLIfSupported();
        setContentView(R.layout.activity_course_deatils);
        fetchArguments();

        dbManager = new DBManager(getApplicationContext());
        courseNotification = new CourseNotification(courseName);
        checkedCourses = new ArrayList<CourseDetailHolder>();
        result = new Intent(getApplicationContext(), MainActivity.class);
        setTitle(courseName);
        setResult(Settings.COURSES, result);
        overridePendingTransition(android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);
        if (savedInstanceState == null) {
            CourseFragment cf = new CourseFragment();

            Bundle extras = new Bundle();
            extras.putString("name", getCourseName());
            extras.putInt("ImageID", getImgId());
            extras.putInt("courseID", getCourseID());
            cf.setArguments(extras);
            extras.putSerializable("course", getIntent().getExtras().getSerializable("course"));
            FragmentTransaction tr = getSupportFragmentManager()
                    .beginTransaction();
            tr.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.add(R.id.container, cf).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.course_deatils, menu);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e1e1e")));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent result = new Intent(getApplicationContext(),
                    MainActivity.class);
            setAlarams();
            result.putExtra("notfiy", courseNotification);
            setResult(Settings.COURSES, result);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public int getParentIndex() {
        return parentIndex;
    }

    public void setParentIndex(int parentIndex) {
        this.parentIndex = parentIndex;
    }

    @Override
    public void onTimeChecked(String time, boolean isChecked) {

        CourseDetailHolder courseDetailHolder=new CourseDetailHolder();
        courseDetailHolder.time=time;
        courseDetailHolder.isChecked=isChecked;

            checkedCourses.add(courseDetailHolder);

        courseNotification.setCourse(setAlarams());
        result.putExtra("notfiy", courseNotification);
        setResult(Settings.COURSES, result);
    }


    private List<Course> setAlarams() {
        List<Course> list = new ArrayList<Course>(checkedCourses.size());
        for (CourseDetailHolder courseDetailHolder : checkedCourses) {
            String[] info = courseDetailHolder.time.split(Pattern.quote(" - "));
            int lastIndex = info.length - 3;
            String day = info[lastIndex - 2];
            String timeFrom = info[lastIndex - 1];
            String tumeTo = info[lastIndex];
            String place = info[lastIndex - 3];
            String notify = info[lastIndex + 1];
            String id = info[lastIndex + 2];
            for (int i = lastIndex - 4; i >= 0; --i) {
                place += " " + info[i];
            }

            Course c = new Course(courseName, day, timeFrom, tumeTo, place);
            c.setNotify(courseDetailHolder.isChecked?1:0);
            c.setCourseID(Integer.parseInt(id));
            list.add(c);

        }


        return list;
    }

    static class CourseDetailHolder{
        public String time;
        public boolean isChecked;
    }
}


/*
 * 
 * 
 * int dayNum=dayWeekNumber(day); int
 * hourFrom=Integer.parseInt(timeFrom.split(":")[0]); int
 * minFrom=Integer.parseInt(timeFrom.split(":")[1]); Calendar calendar =
 * Calendar.getInstance(); calendar.set(Calendar.DAY_OF_WEEK, dayNum);
 * calendar.set(Calendar.HOUR_OF_DAY, hourFrom); calendar.set(Calendar.MINUTE,
 * minFrom-30); calendar.set(Calendar.SECOND, 0); Intent myIntent =new
 * Intent(this, Reciever.class);
 */